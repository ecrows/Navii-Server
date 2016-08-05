package com.navii.server.persistence.dao;

import com.navii.server.persistence.domain.Preference;
import com.navii.server.persistence.domain.UserPreference;

import java.util.List;

/**
 * Created by sjung on 10/11/15.
 */
public interface UserPreferenceDAO {

    /**
     * Saves a new UserPreference entry to the database.
     *
     * @param saved The information of the saved UserPreference entry.
     * @return true if successful, false otherwise
     */
    boolean create(List<Preference> saved);

    /**
     * Return list of preferences based on the email
     *
     * @param username Username of the user
     * @return List of preferences
     */
    List<Preference> obtain(String username);

    /**
     * Deletes all userspreferences from the database based on the email and the type of preference
     *
     * @param preferenceType Identifier for userpreference to be deleted
     * @return Number of rows effected
     */
    int deleteAllPreference(int preferenceType);
}
