package com.project.pod.userRepository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.project.pod.userEntity.bookings;

@Repository
public interface bookingRepository extends JpaRepository<bookings,Long>{

//	@Query("select count(*) from bookings  where trainName=?1 and departure=?2 and arrival=?3 and departureDate=?4")
	@Query("select count(id)from bookings  where trainName=?1 and departure=?2 and arrival=?3 and departureDate=?4")
	public String countBy(String a,String b,String c,String d);
	
	@Query("select u from bookings u where u.email=?1 and u.enable=1")
	public List<bookings> findByEmail(String email);
	
	@Query("select u from bookings u where u.ticketId=?1")
	public bookings findByTID(String ticketId);
	
	@Modifying
	@Transactional
	@Query("update bookings b set b.enable=0 where b.ticketId=?1")
	public void updateEnable(String ticketId);
		
}
