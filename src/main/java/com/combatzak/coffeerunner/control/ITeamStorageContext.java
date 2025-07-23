package com.combatzak.coffeerunner.control;

import com.combatzak.coffeerunner.model.Teammate;

import java.util.Collection;

public interface ITeamStorageContext {

    Collection<Teammate> fetchTeamMembers();
    void saveTeamMembers(Collection<Teammate> team);
}
