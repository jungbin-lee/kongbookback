package com.bookbook.bookback.controller;

import com.bookbook.bookback.config.security.JwtTokenProvider;
import com.bookbook.bookback.controllerReturn.DetailReturn;
import com.bookbook.bookback.controllerReturn.ResultReturn;
import com.bookbook.bookback.domain.dto.TownBookDto;
import com.bookbook.bookback.domain.model.TownBook;
import com.bookbook.bookback.domain.model.User;
import com.bookbook.bookback.domain.repository.UserRepository;
import com.bookbook.bookback.service.FileUploadService;
import com.bookbook.bookback.service.TownBookService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;


@RequiredArgsConstructor
@RestController
public class TownBookController {
    private final TownBookService townBookService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final FileUploadService fileUploadService;
    //동네책장 전체 조회
    @GetMapping("/api/townbooks")
    public ResultReturn getTownBooks(){

        return townBookService.getTownBooks();
    }

    //동네책장에 책 등록
    @PostMapping("/api/townbooks")
    public ResultReturn createTownBook(@RequestPart(required = false) List<MultipartFile> captureFiles, @RequestPart(required = false) String townBookDto) throws JsonProcessingException {
//        String token = jwtTokenProvider.resolveToken(httpServletRequest);
//        String email = jwtTokenProvider.getUserPk(token);
//        User user = userRepository.findByEmail(email).orElseThrow(
//                ()->new IllegalArgumentException("존재하지 않습니다.")
//        );
        System.out.println(townBookDto);

        User user =userRepository.findById(1L).orElseThrow(
                ()->new IllegalArgumentException("존재하지 않습니다.")
        );
        if(captureFiles.isEmpty()){
            return new ResultReturn(false, "file이 존재하지 않습니다");
        }
        List<String> captureImages=new ArrayList<>();
        for(MultipartFile captureFile: captureFiles){
            String captureImage= fileUploadService.uploadImage(captureFile);

            System.out.println(captureImage);

            captureImages.add(captureImage);
        }

        if(townBookDto==null){
            return new ResultReturn(false, "Dto가 존재하지 않습니다");
        }

        ObjectMapper objectMapper = new ObjectMapper();
        TownBookDto dto = objectMapper.readValue(townBookDto, TownBookDto.class);

        return townBookService.createTownBook(user, dto, captureImages);
    }

    //등록한 책 정보 수정
    @PutMapping("/api/townbooks/{townBookId}")
    public ResultReturn updateTownBook(@PathVariable Long townBookId, @RequestBody TownBookDto townBookDto, HttpServletRequest httpServletRequest){
        String token = jwtTokenProvider.resolveToken(httpServletRequest);
        String email = jwtTokenProvider.getUserPk(token);
        User user = userRepository.findByEmail(email).orElseThrow(
                ()->new IllegalArgumentException("존재하지 않습니다.")
        );
        return townBookService.updateTownBook(townBookId,townBookDto, user);
    }

    //등록한 책 삭제
    @DeleteMapping("/api/townbooks/{townBookId}")
    public ResultReturn deleteTownBook(@PathVariable Long townBookId,HttpServletRequest httpServletRequest) {
        String token = jwtTokenProvider.resolveToken(httpServletRequest);
        String email = jwtTokenProvider.getUserPk(token);
        User user = userRepository.findByEmail(email).orElseThrow(
                ()->new IllegalArgumentException("존재하지 않습니다.")
        );
        return townBookService.deleteTownBook(townBookId, user);
    }


    //상세 페이지 정보 조회
    @GetMapping("/api/townbooks/{townBookId}")
    public DetailReturn detail(@PathVariable Long townBookId) {
        return townBookService.detailTownBook(townBookId);
    }

    @GetMapping("/api/townbooks/page")
    public Page<TownBook> getAllBooks(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam("sortBy") String sortBy,
            @RequestParam("isAsc") boolean isAsc
    ) {
        page = page - 1;
        return townBookService.getAllBooks(page , size, sortBy, isAsc);
    }

}
