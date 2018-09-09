package com.hinode.controller;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.hinode.dto.HouseSearchCondition;
import com.hinode.entity.House;
import com.hinode.entity.Image;
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

		// Pre search
		if (condition.getAreaTo() == 0) {
			condition.setAreaTo(MAX_INT);
		}

		if (condition.getRentFeeTo() == 0) {
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
		// Put house
		model.put("house", houseService.getById(id));
		return "public/single-listings";
	}

	@GetMapping("/admin")
	public String admin(Model model, @RequestParam(defaultValue = "0") int page) {
		// Size of list
		int size = 10;
		model.addAttribute("houseList",
				houseService.findAllPagination(page, size, new Sort(Sort.Direction.DESC, "id")));
		model.addAttribute("house", new House());
		model.addAttribute("currentPage", page);
		return "admin/index";
	}

	@GetMapping("/page")
	public String page(Map<String, Object> model) {
		return "admin/pages";
	}

	@PostMapping("/save")
	public String save(HttpServletRequest request) throws Exception {

		@SuppressWarnings("unused")
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		
		DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		House house = new House();
		Class<?> c = house.getClass();
		List<Image> imgList = new ArrayList<>();

		ServletFileUpload upload = new ServletFileUpload();
		FileItemIterator iterStream = upload.getItemIterator(request);
		while (iterStream.hasNext()) {
			FileItemStream item = iterStream.next();
			InputStream stream = item.openStream();
			if (!item.isFormField()) {
				byte[] data = null;
				final ByteArrayOutputStream serializedData = new ByteArrayOutputStream();
	            int b = stream.read();
	            while (b != -1) {
	                serializedData.write(b);
	                b = stream.read();
	            }
	            data = serializedData.toByteArray();
	            Image img = new Image();
	            img.setImageData(data);
	            imgList.add(img);
	            
			} else {
				String name = item.getFieldName();
				String value = Streams.asString(stream);
				Field field = c.getDeclaredField(name);

				if (field.getType().getName().equals("java.time.LocalDate")) {
					LocalDate date = LocalDate.parse(value, format);
					field.setAccessible(true);
					field.set(house, date);
				} else {
					field.setAccessible(true);
					field.set(house, value);
				}
			}
			
            stream.close();
		}
		
		house = houseService.add(house);
		
		for (Image img : imgList) {
			img.setHouseId(house.getId());
			houseService.addImg(img);
		}
		
		return "redirect:/admin";
	}

	@GetMapping("/delete/{id}")
	public String delete(@PathVariable int id) {
		houseService.delete(id);
		return "redirect:/admin";
	}
}
