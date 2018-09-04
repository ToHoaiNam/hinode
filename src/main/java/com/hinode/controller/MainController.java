package com.hinode.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.hinode.dto.HouseSearchCondition;
import com.hinode.entity.House;
import com.hinode.service.HouseService;

@Controller
public class MainController {
	
	private final int MAX_INT = 999999999;
	
	@Autowired
	private HouseService houseService;

	@GetMapping({ "/", "/index" })
	public String init(Map<String, Object> model) {
		
		// Get top 6 new house
		model.put("houseList", houseService.findTopNewHouse());
		model.put("condition", new HouseSearchCondition());
		
		return "public/index";
	}

	@RequestMapping("/listings")
	public String list(Map<String, Object> model, @ModelAttribute HouseSearchCondition condition) {
		model.put("houseList", houseService.findByCondition(condition));
		// Pre search
		if (condition.getAreaTo() == 0) {
			condition.setAreaTo(MAX_INT);
		}
		
		if  (condition.getRentFeeTo() == 0) {
			condition.setRentFeeTo(MAX_INT);
		}
		
		if (condition.getDepositeFeeTo() == 0) {
			condition.setDepositeFeeTo(MAX_INT);
		}
		
		if (condition.getGuaranteeFeeTo() == 0) {
			condition.setGuaranteeFeeTo(MAX_INT);
		}
		
		List<House> houseList = houseService.findByCondition(condition);
		
		model.put("houseList", houseList);
		model.put("condition", new HouseSearchCondition());
		return "public/listings";
	}
	
	@GetMapping("/single")
	public String single(Map<String, Object> model, @RequestParam int id) {
		//Put house
		model.put("house", houseService.getById(id));
		return "public/single-listings";
	}
	
	@GetMapping("/admin")
	public String admin(Map<String, Object> model) {
		// Get top 10 new house
		model.put("houseList", houseService.findLast10House());
		model.put("house", new House());
		return "admin/index";
	}
	
	@GetMapping("/page")
	public String page(Map<String, Object> model) {
		return "admin/pages";
	}
	
	@PostMapping("/save")
	public String save(@ModelAttribute House house) {
		houseService.add(house);
		return "redirect:/admin";
	}

}
