"""Tests for mysqldump_to_pg.py. Run from this folder: python -m unittest test_mysqldump_to_pg"""

import os
import tempfile
import unittest

from mysqldump_to_pg import convert

DUMP = r"""-- MySQL dump 10.13
CREATE TABLE `customer` (
  `customer_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `address` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`customer_id`)
) ENGINE=InnoDB AUTO_INCREMENT=50 DEFAULT CHARSET=utf8mb3;
CREATE TABLE `payment` (
  `payment_id` int NOT NULL AUTO_INCREMENT,
  `amount` decimal(10,2) NOT NULL,
  `remarks` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`payment_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb3;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `enabled` bit(1) NOT NULL,
  `username` varchar(45) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb3;
INSERT INTO `customer` VALUES (1,'Biker\'s Choice','#1 St.\\Unit\t2'),(2,'Line\nBreak',NULL);
INSERT INTO `customer` VALUES (3,'It''s quoted','a, (b); c');
INSERT INTO `payment` (`payment_id`,`amount`,`remarks`) VALUES (1,100.10,'cash'),(2,0.20,NULL);
INSERT INTO `users` VALUES (1,_binary '\x01','admin'),(2,_binary '\0','old');
"""


class ConvertTest(unittest.TestCase):

    def setUp(self):
        self.dir = tempfile.mkdtemp()
        self.dump = os.path.join(self.dir, "dump.sql")
        self.out = os.path.join(self.dir, "out.sql")
        with open(self.dump, "w", encoding="utf-8", newline="\n") as f:
            # \x01 stands for the raw byte mysqldump writes for bit(1) true
            f.write(DUMP.replace(r"\x01", "\x01"))
        convert(self.dump, self.out)
        with open(self.out, encoding="utf-8") as f:
            self.sql = f.read()

    def copy_rows(self, table):
        block = self.sql.split("COPY %s " % table, 1)[1]
        return block.split("\n\\.\n", 1)[0].split("\n")[1:]

    def test_strings_are_unescaped_from_mysql_and_escaped_for_copy(self):
        self.assertEqual(self.copy_rows("customer"), [
            "1\tBiker's Choice\t#1 St.\\\\Unit\\t2",
            "2\tLine\\nBreak\t\\N",
            "3\tIt's quoted\ta, (b); c",
        ])

    def test_explicit_column_lists_are_kept(self):
        self.assertIn("COPY payment (payment_id, amount, remarks) FROM stdin;", self.sql)
        self.assertEqual(self.copy_rows("payment"), ["1\t100.10\tcash", "2\t0.20\t\\N"])

    def test_bit_columns_become_booleans(self):
        self.assertEqual(self.copy_rows("users"), ["1\tt\tadmin", "2\tf\told"])

    def test_sequences_continue_after_mysql_auto_increment(self):
        self.assertIn("setval(pg_get_serial_sequence('customer', 'customer_id'), "
                      "GREATEST(COALESCE((SELECT max(customer_id) FROM customer), 0), 49, 1))", self.sql)

    def test_load_is_checked_and_atomic(self):
        self.assertIn("IF (SELECT count(*) FROM customer) <> 3 THEN", self.sql)
        # Decimal sum, not float: 100.10 + 0.20 must be exactly 100.30.
        self.assertIn("IF (SELECT COALESCE(sum(amount), 0) FROM payment) <> 100.30 THEN", self.sql)
        self.assertLess(self.sql.index("BEGIN;"), self.sql.index("COPY "))
        self.assertLess(self.sql.index("END $$;"), self.sql.index("COMMIT;"))
        self.assertTrue(self.sql.rstrip().endswith("ANALYZE;"))


if __name__ == "__main__":
    unittest.main()
