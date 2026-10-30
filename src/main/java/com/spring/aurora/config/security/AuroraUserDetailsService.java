package com.spring.aurora.config.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.spring.aurora.dao.UserDao;

@Service
public class AuroraUserDetailsService implements UserDetailsService {

    private UserDao userDao;

    public AuroraUserDetailsService(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username != null) {
            if (username.indexOf("@") != -1) {
                return this.userDao.findByEmailIgnoreCase(username)
                        .orElseThrow(() -> new UsernameNotFoundException("Email not found."));
            } else {
                return this.userDao.findByUsernameIgnoreCase(username)
                        .orElseThrow(() -> new UsernameNotFoundException("Username not found."));
            }
        }
        throw new UsernameNotFoundException("Username not found.");
    }

}
