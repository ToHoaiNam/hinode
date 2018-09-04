package com.hinode.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hinode.dao.IHouseDAO;
import com.hinode.dto.HouseSearchCondition;
import com.hinode.entity.House;

@Service
public class HouseService{

	@Autowired
	private IHouseDAO houseDAO;
	
	public void add(House house) {
		houseDAO.save(house);
	}
	
	public List<House> findByCondition(HouseSearchCondition condition) {
		
		return null;
	}
	
	public List<House> findTopNewHouse() {
		return houseDAO.fetchTop6();
	}
	
	public List<House> findLast10House() {
		return houseDAO.fetchLast10();
	}
	
	public House getById(int id) {
		return houseDAO.getOne(id);
	}
}
