package com.spring.jpa.chap05_practice.service;

import com.spring.jpa.chap05_practice.dto.*;
import com.spring.jpa.chap05_practice.entity.HashTag;
import com.spring.jpa.chap05_practice.entity.Post;
import com.spring.jpa.chap05_practice.repository.HashTagRopository;
import com.spring.jpa.chap05_practice.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final HashTagRopository hashTagRopository;

    public PostListResponseDTO getPosts(PageDTO dto) {

        Pageable pageable = PageRequest.of(dto.getPage()-1,dto.getSize(),
                Sort.by("createDate").descending());

        Page<Post> posts = postRepository.findAll(pageable);

        // 게시물 정보만 꺼내기
        List<Post> postList = posts.getContent();

        List<PostDetailResponseDTO> detailList
                = postList.stream().map(post -> new PostDetailResponseDTO(post))
                .collect(Collectors.toList());

        PostListResponseDTO responseDTO = PostListResponseDTO.builder()
                .count(detailList.size()).pageInfo(new PageResponseDTO(posts)).posts(detailList).build();

        return responseDTO;
    }

    public PostDetailResponseDTO getDetail(long id) {
        Post postEntity = getPost(id);

        return  new PostDetailResponseDTO(postEntity);
    }

    private Post getPost(long id) {
        Post postEntity = postRepository.findById(id).orElseThrow(
                () -> new RuntimeException(id + "번 게시물이 존재하지 않습니다.")
        );
        return postEntity;
    }

    public PostDetailResponseDTO insert(final PostCreateDTO dto)
        throws RuntimeException {
        Post saved = postRepository.save(dto.toEntity());

        List<String> hashTags = dto.getHashTags();
        if(hashTags != null & hashTags.size() > 0){
            hashTags.forEach(ht -> {
                HashTag savedTag = hashTagRopository.save(
                        HashTag.builder().tagName(ht).post(saved).build()
                );
                saved.addHashTag(savedTag);
            });

        }

        return new PostDetailResponseDTO(saved);
    }

    public PostDetailResponseDTO modify(PostModifyDTO dto) {

        // 수정 전 데이터를 조회
        Post postEntity = getPost(dto.getPostNo());

        // 수정 시작
        postEntity.setTitle(dto.getTitle());
        postEntity.setContent(dto.getContent());

        // 수정 완료
        Post modifiedPost = postRepository.save(postEntity);

        return new PostDetailResponseDTO(modifiedPost);
    }

    public void detele(long id) {
        postRepository.deleteById(id);
    }
}
