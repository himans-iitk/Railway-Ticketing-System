 package com.project.pod.controller;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.project.pod.service_layer.admin_service;
import com.project.pod.service_layer.service_layer;
import com.project.pod.userEntity.Train;
import com.project.pod.userEntity.bookings;
import com.project.pod.userEntity.users;
import com.project.pod.userRepository.trainRepository;
import com.project.pod.websecurity.User;
import com.project.pod.websecurity.WebRepository;
import com.univocity.parsers.common.record.Record;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

@Controller
public class AdminController {
     
	@Autowired
	private trainRepository trepo;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private service_layer service;
	
	@Autowired
	private admin_service aservice;
	
	@Autowired
	private WebRepository wrepo;
	
	@Autowired
	private JavaMailSender mailsender;
	
	
	@GetMapping("/admin")
	public String admin(Model model) {
		User k=new User();
		model.addAttribute("User",k);
		return "admin_login";
	}
	
	
	@PostMapping("/admin") 
	  public ModelAndView admin(@ModelAttribute User user,HttpServletRequest request) {
	         ModelAndView mav; HttpSession session=request.getSession();
	         if(service.adminCheck(user)==false) { mav=new ModelAndView("admin_login");mav.addObject("User",user);
	         
	         session.setAttribute("message","login fail.please check your credentails");
	          return mav; }
	         else {
	        	 
	        	 session.setAttribute("adminUserName",user.getUsername());
	        	 String p=(String)session.getAttribute("adminUserName");session.setMaxInactiveInterval(60);
	        	 
	        	 mav=new ModelAndView("admin_login_success");mav.addObject("message","Welcome "+p);return mav;}
	         }
	
	

	
	
	@GetMapping("/upload")
	public String upload(Model model) {
		return "upload_train_data";
	}
	
	
	@PostMapping("/upload")
	public String uploadData(@RequestParam("file") MultipartFile file) throws Exception{
		
		
		List<Train> trains=new ArrayList<>();
		
		InputStream inputStream=file.getInputStream();
		CsvParserSettings setting=new CsvParserSettings();
		setting.setHeaderExtractionEnabled(true);
		CsvParser parser=new CsvParser(setting);
		List<Record> parseAllRecords=parser.parseAllRecords(inputStream);
		parseAllRecords.forEach(record->{
			
			Train train=new Train();
			train.setName(record.getString("name"));
			train.setFromStation(record.getString("fromStation"));
			train.setToStation(record.getString("toStation"));
			train.setDepartureTime(record.getString("departureTime"));
			train.setArrivalTime(record.getString("arrivalTime"));
			train.setEnable(aservice.getBoolean(record.getString("enable")));
			
			trains.add(train);
		});
		
		trepo.saveAll(trains);
		return "upload succesfull";
	}
	
	
	public boolean getBoolean(String value) {
		
		if(value.equals("1")) {return true;}
		return false;
	}
	
	@GetMapping("/TrainsSchedule")
	public ModelAndView showtrains() {
		ModelAndView mav=new ModelAndView("showtrains");
		mav.addObject("trains",trepo.findAll());
		return mav;
	}
	
	
	@GetMapping("/addTrain")
	public ModelAndView addTrain() {
		ModelAndView mav=new ModelAndView("addTrain");
		Train train=new Train();
		mav.addObject("train",train);
		return mav;
	}
	
	@PostMapping("/addTrain")
	public String saveEmployee(@ModelAttribute("train") Train train) {
		train.setEnable(true);
		trepo.save(train);
		return "redirect:/TrainsSchedule";
	}
	
	@GetMapping("/showUpdateForm")
	public ModelAndView showUpdateForm(@RequestParam Long trainId) {
		ModelAndView mav=new ModelAndView("addTrain");
		Train train=trepo.findById(trainId).get();
		mav.addObject("train",train);
		return mav;
	}
	
	@GetMapping("/DeleteRow")
	public String deleteById(@RequestParam Long trainId) {
		trepo.deleteById(trainId);
		return "redirect:/TrainsSchedule";
	}
	
	@GetMapping("/DeleteTrains")
	public String deleteTrains() {
		trepo.deleteAll();
		return "redirect:/TrainsSchedule";
	}
	
	
	public String checkStation(Model model) {
		bookings ticket=new bookings();
		model.addAttribute("ticket",ticket);
		return "checkStation";
	}
	
	
	@GetMapping("/adminlogout")
	public String adminlogout(HttpSession session) {
		session.removeAttribute("adminUserName");
		session.removeAttribute("message");
		return "redirect:/admin";
	}
	
	
	@GetMapping("/adminprofile")
	public String adminprofile(Model model,HttpSession session) {
		
		String p=(String)session.getAttribute("adminUserName");
		User k=(User)wrepo.findByUsername(p);
		model.addAttribute("k",k);
		return "adminprofile";
		
		
		
	}
	
	@PostMapping("/adminprofile")
	public ModelAndView adminprofile(@ModelAttribute("k") User k,HttpSession session) {
		ModelAndView mav=new ModelAndView("adminprofile");
		String m=passwordEncoder.encode(k.getPassword());
		wrepo.updatePass(k.getUsername(),m);
		session.setAttribute("message","profile saved succesfully");
		mav.addObject("k",k);
		return mav;
		
	}
	
	
	@GetMapping("/contactus")
	public String contactus() {
		return "contactus";
	}
	
	@PostMapping("/contactus")
	public String contactus (HttpSession session,@RequestParam("email") String a,@RequestParam("enquiry") String b)throws MessagingException,UnsupportedEncodingException {
		sendmail(a,b);
		session.setAttribute("message", "Enquiry sent successfully");
		return "contactus";
	}
	
	private void sendmail(String a,String b) throws MessagingException,UnsupportedEncodingException{
		String toAddress=a;
		String fromAddress="pod05project@gmail.com";
		String senderName="Himanshu_Mishra";
		String subject="Enquiry";
		
		String content="Dear Customer <br/>,We have received your enquiry,We will get back to you.<br/>"+"Thank you <br/>"+senderName;
		
		
		
		
		MimeMessage message=mailsender.createMimeMessage();
		MimeMessageHelper helper=new MimeMessageHelper(message);
		
		helper.setFrom(fromAddress,senderName);
		helper.setTo(toAddress);
		helper.setSubject(subject);
		
		helper.setText(content,true);
		mailsender.send(message);
		
		
	}
   
	
	
	
	
	
}
