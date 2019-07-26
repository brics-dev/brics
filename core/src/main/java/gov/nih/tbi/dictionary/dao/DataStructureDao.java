
package gov.nih.tbi.dictionary.dao;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.commons.model.StatusType;
import gov.nih.tbi.commons.util.PaginationData;
import gov.nih.tbi.dictionary.model.hibernate.FormStructure;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DataStructureDao// extends GenericDao<FormStructure, Long>
{/*

    public FormStructure get(String shortName, Integer version);

    *//**
     * Returns the form structure with the given shortname. If multiple with the same shortName exist, then returns the
     * one with the highest version number.
     * 
     * @param shortName
     * @return
     *//*
    public FormStructure getLatestVersionByShortName(String shortName);

    *//**
     * Returns all the form structures in a map of it's primary key as the key to the object as the value
     * @return
     *//*
    public Map<Long, FormStructure> getAllIntoMap();

    *//**
     * Returns a list of datastructure with the ID that exist in dsIdList
     * 
     * @param dsIdList
     * @return
     *//*
    public List<FormStructure> getAllSortedById(List<Long> dsIdList, PaginationData pageData);

    public List<FormStructure> getAllById(List<Long> dsIdList);

    public List<FormStructure> listDataStructuresByStatus(StatusType status);

    public List<FormStructure> listDataStructures(Set<Long> ids, PaginationData pageData);

    public List<FormStructure> filterDataStructures(Set<Long> ids, Long filterType, PaginationData pageData);

*/}
