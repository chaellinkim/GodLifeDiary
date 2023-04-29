package com.gld.model.biz;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gld.model.dto.ChallengeDto;
import com.gld.model.repository.ChallengeRepository;

@Service
public class ChallengeBiz {
   
   @Autowired
   private ChallengeRepository challengeRepository;
   
   public void insert(ChallengeDto dto) {
      
      challengeRepository.save(dto);
   }
   
   public List<ChallengeDto> selectAll() {
       
       return challengeRepository.findAll();
   }
   
}