package com.gld.model.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gld.model.biz.ChallengeBiz;
import com.gld.model.biz.CommentBiz;
import com.gld.model.biz.LoginBiz;
import com.gld.model.biz.RegisteredBiz;
import com.gld.model.dto.ChallengeDto;
import com.gld.model.dto.CommentDto;
import com.gld.model.dto.CommentId;
import com.gld.model.dto.UserDto;

@Controller
@RequestMapping("/challenge")
public class ChallengeController {

	@Autowired
	private ChallengeBiz challengeBiz;

	@Autowired
	private RegisteredBiz registeredBiz;

	@Autowired
	private LoginBiz loginBiz;
	
	@Autowired
	private CommentBiz commentBiz;


	@GetMapping("/main")
	public String getAllChallenges(Model model) {
		// 모든 Challenge 엔티티 조회
		List<ChallengeDto> challenges = challengeBiz.selectAll();
		// 조회된 엔티티들을 모델에 담아서 뷰로 전달
		model.addAttribute("challenges", challenges);
		return "main";

	}



	@GetMapping("/main_study")
	public String getStudyChallenges(Model model) {
		List<ChallengeDto> challenges = challengeBiz.findbyCate("공부");
		model.addAttribute("challenges", challenges);
		return "main";
	} 


	@GetMapping("/main_habit")
	public String getHabitChallenges(Model model) {
		List<ChallengeDto> challenges = challengeBiz.findbyCate("습관");
		model.addAttribute("challenges", challenges);
		return "main";
}

	@GetMapping("/main_hobby")
	public String getHobbyChallenges(Model model) {
		List<ChallengeDto> challenges = challengeBiz.findbyCate("취미");
		model.addAttribute("challenges", challenges);
		return "main";
}

	@GetMapping("/main_workout")
	public String getWorkoutChallenges(Model model) {
		List<ChallengeDto> challenges = challengeBiz.findbyCate("운동");
		model.addAttribute("challenges", challenges);
		return "main";
}

	@GetMapping("/detail")
	public String moveToDetail(Model model, String challengeName) {
		ChallengeDto challenge = challengeBiz.selectOne(challengeName);

		model.addAttribute("challenge", challenge);

		return "challengedetail";
	}

	@GetMapping("/insert")
	public String insert() {
		return "challengeinsert";
	}

	@PostMapping("/challengeinsert")
	public String challengeInsert(ChallengeDto dto) {
		challengeBiz.insert(dto);
		return "challengeinsert_res";
	}
	
	 @PostMapping("/ajaxComment")
	   @ResponseBody
	   public Map<String, Object> commentDate(@RequestBody CommentId commentid) {
		   System.out.println(commentid.getSeq()+" " +commentid.getId()+" "+commentid.getCommentDate());
		   Map<String, Object> res = new HashMap<>();
		   
		   
		   CommentDto comment = commentBiz.selectComment(commentid.getSeq(), commentid.getId(), commentid.getCommentDate());
		   List<CommentDto> list = commentBiz.selectComments(commentid.getSeq(), commentid.getCommentDate());
		   //System.out.println(comment.getId());
		   System.out.println(list.get(2).getComment());
		   Map<String, CommentDto> map = new HashMap<>();
		   
		   if(comment != null) {
			   map.put("comment", comment);
		   }else {
			   map.put("comment", null);
		   }
		   
		   res.put("comment", map);
		   res.put("list",list);
		   
		   return res;
	   }

	// 참여하기 버튼 눌렀을때 로직
	@RequestMapping(value = "/joinuser", method = RequestMethod.POST)
	public @ResponseBody String joinUser(@RequestBody String json) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			// JSON 문자열을 Java 객체로 변환
			Map<String, Object> map = mapper.readValue(json, new TypeReference<Map<String, Object>>() {
			});

			// Java 객체에서 값을 추출하여 변수에 할당
			String challengeSeq = map.get("challengeSeq").toString();
			String userId = map.get("userId").toString();

			// 필요한 DTO, 첼린지별 사람 준비
			ChallengeDto currentChallenge = challengeBiz.selectOneBySeq(challengeSeq);
			UserDto currentUser = loginBiz.findByUserId(userId);
			int currentMember = registeredBiz.coutBySeq(challengeSeq);
			
			System.out.println("필요한 정보 로딩 완료\n" + currentChallenge + "\n" + currentUser + "\n" + currentMember);

			// 비교 후 디비에 넣기
			if (currentChallenge.getChallengeEnabled().equals("Y")
					&& currentMember < currentChallenge.getChallengeMaxMember()) {
				int res = registeredBiz.insert(challengeSeq, currentUser.getId());
				System.out.println("controller insert res: " + res);
				
				// 디비 반영 후 맥스맴버와 커랜트 맴버 비교하기
				int member = registeredBiz.coutBySeq(challengeSeq);
				// 비교 후 둘이 같으면 첼린지 시작
				if(currentChallenge.getChallengeMaxMember() <= member) {
					System.out.println("넘아갔다. \n" + member + "\n" + currentChallenge.getChallengeMaxMember());
					registeredBiz.challengeStart(challengeSeq);
				}
				
				if (res > 0) {
					return currentChallenge.getChallengeName() + " 에 참여하였습니다. ";
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "참여에 실패하였습니다. 다시 시도해주세요 ";// 클라이언트로 반환할 데이터
		}
		return "error 발생";
	}
}