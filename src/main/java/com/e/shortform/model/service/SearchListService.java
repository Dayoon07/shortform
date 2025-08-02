package com.e.shortform.model.service;

import com.e.shortform.model.repository.SearchListRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchListService {

    private final SearchListRepo searchListRepo;

    public void searchWordRecord(String q) {

    }


}
