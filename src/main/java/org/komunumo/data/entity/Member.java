package org.komunumo.data.entity;

import javax.persistence.Entity;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.komunumo.data.AbstractEntity;
import java.time.LocalDate;

@Entity
public class Member extends AbstractEntity {

    private String firstName;
    private String lastName;
    private String email;
    private String address;
    private String zipCode;
    private String city;
    private String state;
    private String country;
    private LocalDate memberSince;
    private boolean admin;
    private String passwordSalt;
    private String passwordHash;
    private String activationCode;
    private boolean active;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(final String address) {
        this.address = address;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(final String zipCode) {
        this.zipCode = zipCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(final String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(final String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(final String country) {
        this.country = country;
    }

    public LocalDate getMemberSince() {
        return memberSince;
    }

    public void setMemberSince(final LocalDate memberSince) {
        this.memberSince = memberSince;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(final boolean admin) {
        this.admin = admin;
    }

    public String getPasswordSalt() {
        return passwordSalt;
    }

    public void setPasswordSalt(final String passwordSalt) {
        this.passwordSalt = passwordSalt;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(final String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setPassword(final String password) {
        passwordSalt = RandomStringUtils.random(32);
        passwordHash = DigestUtils.sha1Hex(password + passwordSalt);
    }

    public boolean checkPassword(final String password) {
        return DigestUtils.sha1Hex(password + passwordSalt).equals(passwordHash);
    }

    public String getActivationCode() {
        return activationCode;
    }

    public void setActivationCode(final String activationCode) {
        this.activationCode = activationCode;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

}
