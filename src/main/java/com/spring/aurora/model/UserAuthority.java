package com.spring.aurora.model;

import static javax.persistence.GenerationType.IDENTITY;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

//@Entity
//@Table(name = "authorities", catalog = "aurora", uniqueConstraints = @UniqueConstraint(columnNames = { "username", "authority" }))
public class UserAuthority implements Serializable {

    private static final long serialVersionUID = 8031993448564449422L;
    private Integer id;
    private User user;
    private String authority;

    public UserAuthority() {
    }

    public UserAuthority(User user, String role) {
        this.user = user;
        this.authority = role;
    }

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    public Integer getUserRoleId() {
        return this.id;
    }

    public void setUserRoleId(Integer userRoleId) {
        this.id = userRoleId;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "username", nullable = false)
    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Column(name = "authority", nullable = false, length = 45)
    public String getAuthority() {
        return this.authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

}