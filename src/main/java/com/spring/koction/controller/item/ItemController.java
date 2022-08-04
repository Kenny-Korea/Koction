package com.spring.koction.controller.item;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.codehaus.groovy.syntax.Numbers;
import org.springframework.beans.factory.annotation.Autowired;

import com.spring.koction.entity.CustomUserDetails;
import com.spring.koction.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.spring.koction.commons.FileUtils;
import com.spring.koction.entity.Item;
import com.spring.koction.entity.ItemFile;
import com.spring.koction.entity.Itemq;
import com.spring.koction.service.item.ItemService;
import com.spring.koction.service.user.UserService;

@RestController
@RequestMapping("/item")
public class ItemController {
	@Autowired
	ItemService itemService;

	@Autowired
	UserService userService;

	//내 아이템 조회 /item/myItem
	@GetMapping("")
	public ModelAndView myItem(Item item, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
		ModelAndView mv = new ModelAndView();
		mv.setViewName("item/myItem.html");
		String test = customUserDetails.getUsername();
		List<Item> myItemList = itemService.getMyItemList(test);
		for(Item item1:myItemList) {
			if(itemService.findItemFilesByItemNo(item1.getItemNo()).size() != 0) {
				item1.setItemFile(itemService.findItemFilesByItemNo(item1.getItemNo()).get(0));
			}
		}
		mv.addObject("itemList", myItemList);
//		mv.addObject("itemFile", myItemFile);

		return mv;
	}

	@GetMapping("/registerItem")
	public ModelAndView registerItemView() {
		ModelAndView mv = new ModelAndView();
		mv.setViewName("/item/itemRegister.html");
		return mv;
	}
	
	@PostMapping("/registerItem")
	public ModelAndView registerItem(Item item, int term, HttpServletRequest request, MultipartHttpServletRequest multipartServletRequest) throws IOException {
		item.setItemEnddate(item.getItemRegdate().plusDays(term));
		int itemNo = itemService.registerItem(item);//글등록 및 글 번호 반환
		
		FileUtils fileUtils = new FileUtils();
		List<ItemFile> fileList = fileUtils.parseFileInfo(itemNo, request, multipartServletRequest);
		
		for(ItemFile itemFile : fileList) {
			System.out.println(itemFile.getItem());
		}
		itemService.registerItemFile(fileList);
		
		ModelAndView mv = new ModelAndView();
		mv.setViewName("redirect:/item"); // myitem이 아니라 item으로 보내야해서 수정함
		return mv;
	}
	
	@PostMapping("/updateItem")
	public void updateItem(Item item, HttpServletResponse httpServletResponse, HttpServletRequest httpServletRequest, MultipartHttpServletRequest multipartHttpServletRequest) throws IOException {
		itemService.updateItem(item);
		FileUtils fileUtils = new FileUtils();
		List<ItemFile> fileList = fileUtils.parseFileInfo(item.getItemNo(), httpServletRequest, multipartHttpServletRequest);
		itemService.registerItemFile(fileList);
		httpServletResponse.sendRedirect("/item/myItem");
	}

	@GetMapping("/search")
	public ModelAndView searchView() {
		ModelAndView mv = new ModelAndView();
		mv.setViewName("/item/Search.html");


		return mv;
	}
	@GetMapping("/test")
	public ModelAndView testViewOrigin(@PathVariable int itemNo) {
		ModelAndView mv = new ModelAndView();
		List<Itemq> list = itemService.selectInquryList();
		mv.addObject("list",list);
		mv.setViewName("/item/ProductInfo");
		return mv;
	}

	@GetMapping("/test/{itemNo}")
	public ModelAndView testView(@PathVariable int itemNo) {
		ModelAndView mv = new ModelAndView();

		List<Itemq> list = itemService.selectInquryList();
		for(Itemq itemq : list) {
			System.out.println(itemq.toString());
		}

		mv.addObject("list",list);
		mv.addObject("itemNo", itemNo);
//		System.out.println("itemNo////////////////////////"+itemNo);
		mv.setViewName("/item/ProductInfo");
		return mv;
	}

	@PostMapping("/inquiry")
	public ModelAndView testPost(Itemq itemq) {
		ModelAndView mv = new ModelAndView();
//		itemq.getItem().setItemNo(itemNo);


//		System.out.println("itemNo////////////////////////"+itemNo);
//		System.out.println("itemq////////////////////////"+itemq.getItem().getItemNo());
//		System.out.println("itemq////////////////////////"+itemq);

		System.out.println(itemq.getItem());

		int itemqNo = itemService.insertInqury(itemq);
		mv.setViewName("redirect:/item/searchItem/{itemNo}");
		return mv;
	}


	@PostMapping("/test/deleteTest")
	public void deleteTest(@RequestParam int itemqNo, @RequestParam int itemNo){
		System.out.println("itemqNo========================================================="+itemqNo);
		itemService.deleteTest(itemqNo, itemNo);
  	}
	@GetMapping("/searchItem/{itemNo}")
	public ModelAndView searchItemView(@PathVariable int itemNo) {
		ModelAndView mv = new ModelAndView();
		mv.setViewName("/item/ProductInfo.html");
		
		Item item = itemService.getItem(itemNo);
		List<ItemFile> fileList = itemService.getItemFileList(itemNo);
		
		mv.addObject("item", item);
		mv.addObject("fileList", fileList);
		
		itemService.updateItemCnt(itemNo);
		
		return mv;
	}
}
