package nhantr.musicapp.service;

import nhantr.musicapp.dto.response.SearchResponse;

public interface SearchService {

    SearchResponse search(String query, int page, int size);
}
