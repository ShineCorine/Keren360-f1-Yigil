package kr.co.yigil.member.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import kr.co.yigil.file.FileUploadEvent;
import kr.co.yigil.follow.domain.FollowCount;
import kr.co.yigil.global.exception.BadRequestException;
import kr.co.yigil.member.domain.Member;
import kr.co.yigil.member.domain.SocialLoginType;
import kr.co.yigil.member.domain.repository.MemberRepository;
import kr.co.yigil.member.dto.request.MemberUpdateRequest;
import kr.co.yigil.member.dto.response.MemberDeleteResponse;
import kr.co.yigil.member.dto.response.MemberInfoResponse;
import kr.co.yigil.member.dto.response.MemberUpdateResponse;
import kr.co.yigil.post.domain.Post;
import kr.co.yigil.post.domain.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mock.web.MockMultipartFile;

public class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private MemberService memberService;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @DisplayName("getMemberInfo 메서드에 유효한 사용자ID가 주어졌을 때 사용자 정보가 잘 반환되는지")
    @Test
    void whenGetMemberInfo_thenReturnsMemberInfoResponse_withValidMemberInfo() {
        Long memberId = 1L;
        Member mockMember = new Member("kiit0901@gmail.com", "123456", "stone", "profile.jpg", "kakao");
        List<Post> mockPostList = new ArrayList<>();
        FollowCount mockFollowCount = new FollowCount(1L, 0, 0);
        ValueOperations<String, Object> valueOperationsMock = mock(ValueOperations.class);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(mockMember));
        when(postRepository.findAllByMember(mockMember)).thenReturn(mockPostList);
        when(redisTemplate.opsForValue()).thenReturn(valueOperationsMock);
        when(valueOperationsMock.get(anyString())).thenReturn(mockFollowCount);

        MemberInfoResponse response = memberService.getMemberInfo(memberId);

        assertThat(response).isNotNull();
        assertThat(response.getNickname()).isEqualTo("stone");
        assertThat(response.getProfileImageUrl()).isEqualTo("profile.jpg");
    }

    @DisplayName("getMemberInfo 메서드에 잘못된 사용자ID가 주어졌을 때 예외가 잘 발생하는지")
    @Test
    void whenGetMemberInfo_thenThrowsException_withInvalidMemberInfo() {
        Long invalidMemberId = 2L;

        when(memberRepository.findById(invalidMemberId)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> memberService.getMemberInfo(invalidMemberId));
    }

    @DisplayName("updateMemberInfo 메서드가 유효한 사용자 Id가 주어졌을 때 이벤트가 잘 발행되는지")
    @Test
    void whenUpdateMemberInfo_thenReturnsUpdateInfoAndPublishEvent_withValidMemberInfo() {
        Long validMemberId = 1L;
        MemberUpdateRequest request = mock(MemberUpdateRequest.class);
        MockMultipartFile file = new MockMultipartFile("file", "filename.jpg", "image/jpeg", new byte[10]);
        when(request.getProfileImageFile()).thenReturn(file);

        Member mockMember = new Member(1L, "kiit0901@gmail.com", "123456", "stone", "profile.jpg", SocialLoginType.KAKAO);
        when(memberRepository.findById(validMemberId)).thenReturn(Optional.of(mockMember));
        when(memberRepository.save(mockMember)).thenReturn(mockMember);

        MemberUpdateResponse response = memberService.updateMemberInfo(validMemberId, request);

        verify(applicationEventPublisher).publishEvent(any(FileUploadEvent.class));
        assertEquals("회원 정보 업데이트 성공", response.getMessage());
    }

    @DisplayName("updateMemberInfo 메서드가 유효하지 않은 사용자 Id가 주어졌을 때 예외가 잘 발생하는지")
    @Test
    void whenUpdateMemberInfo_thenThrowsException_withInvalidMemberInfo() {
        Long invalidMemberId = 1L;
        MemberUpdateRequest request = new MemberUpdateRequest();
        when(memberRepository.findById(invalidMemberId)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> memberService.updateMemberInfo(invalidMemberId, request));
    }

    @DisplayName("withdraw 메서드가 유효한 사용자 ID가 주어졌을 때 회원 탈퇴가 잘 동작하는지")
    @Test
    void whenWithdraw_shouldDeleteMember_withValidMemberInfo() {
        Long validMemberId = 1L;
        Member mockMember = new Member(1L, "kiit0901@gmail.com", "123456", "stone", "profile.jpg", SocialLoginType.KAKAO);
        when(memberRepository.findById(validMemberId)).thenReturn(Optional.of(mockMember));

        MemberDeleteResponse response = memberService.withdraw(validMemberId);

        verify(memberRepository).delete(mockMember);
        assertEquals("회원 탈퇴 성공",  response.getMessage());
    }

    @DisplayName("withdraw 메서드가 유효하지 않은 사용자 ID가 주어졌을 때 예외를 잘 발생시키는지")
    @Test
    void whenWithdraw_shouldThrowException_withInvalidMemberInfo() {
        Long invalidMemberId = 1L;
        when(memberRepository.findById(invalidMemberId)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> memberService.withdraw(invalidMemberId));
    }

}
