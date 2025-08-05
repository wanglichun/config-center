package com.example.configcenter.service;

import com.example.configcenter.log.LogRecord;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 日志检索服务
 */
@Slf4j
@Service
public class LogSearchService {
    
    @Autowired
    private RestHighLevelClient elasticsearchClient;
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * 按TraceId检索日志
     */
    public List<LogRecord> searchByTraceId(String traceId) {
        try {
            String indexName = "logs-" + LocalDateTime.now().format(FORMATTER);
            SearchRequest searchRequest = new SearchRequest(indexName);
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            
            sourceBuilder.query(QueryBuilders.termQuery("traceId", traceId));
            sourceBuilder.sort("timestamp", SortOrder.DESC);
            sourceBuilder.size(1000);
            
            searchRequest.source(sourceBuilder);
            SearchResponse response = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT);
            
            return parseSearchResponse(response);
        } catch (IOException e) {
            log.error("检索日志失败: traceId={}", traceId, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 按条件检索日志
     */
    public List<LogRecord> searchLogs(String traceId, String category, String operation, 
                                     String startTime, String endTime, int size) {
        try {
            String indexName = "logs-" + LocalDateTime.now().format(FORMATTER);
            SearchRequest searchRequest = new SearchRequest(indexName);
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            
            if (traceId != null && !traceId.isEmpty()) {
                boolQuery.must(QueryBuilders.termQuery("traceId", traceId));
            }
            
            if (category != null && !category.isEmpty()) {
                boolQuery.must(QueryBuilders.termQuery("category", category));
            }
            
            if (operation != null && !operation.isEmpty()) {
                boolQuery.must(QueryBuilders.termQuery("operation", operation));
            }
            
            if (startTime != null && endTime != null) {
                boolQuery.must(QueryBuilders.rangeQuery("timestamp")
                        .gte(startTime)
                        .lte(endTime));
            }
            
            sourceBuilder.query(boolQuery);
            sourceBuilder.sort("timestamp", SortOrder.DESC);
            sourceBuilder.size(size);
            
            searchRequest.source(sourceBuilder);
            SearchResponse response = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT);
            
            return parseSearchResponse(response);
        } catch (IOException e) {
            log.error("检索日志失败", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 解析搜索结果
     */
    private List<LogRecord> parseSearchResponse(SearchResponse response) {
        List<LogRecord> logs = new ArrayList<>();
        
        for (SearchHit hit : response.getHits().getHits()) {
            Map<String, Object> source = hit.getSourceAsMap();
            LogRecord logRecord = new LogRecord();
            
            logRecord.setTimestamp((String) source.get("timestamp"));
            logRecord.setLevel((String) source.get("level"));
            logRecord.setCategory((String) source.get("category"));
            logRecord.setTraceId((String) source.get("traceId"));
            logRecord.setSpanId((String) source.get("spanId"));
            logRecord.setParentSpanId((String) source.get("parentSpanId"));
            logRecord.setOperation((String) source.get("operation"));

            if (source.get("startTime") != null) {
                logRecord.setStartTime(Long.parseLong(source.get("startTime").toString()));
            }
            
            if (source.get("endTime") != null) {
                logRecord.setEndTime(Long.parseLong(source.get("endTime").toString()));
            }
            
            if (source.get("duration") != null) {
                logRecord.setDuration(Long.parseLong(source.get("duration").toString()));
            }
            
            if (source.get("data") != null) {
                logRecord.setData((Map<String, Object>) source.get("data"));
            }
            
            if (source.get("tags") != null) {
                logRecord.setTags((Map<String, String>) source.get("tags"));
            }
            
            logRecord.setErrorMessage((String) source.get("errorMessage"));
            logRecord.setStackTrace((String) source.get("stackTrace"));
            
            logs.add(logRecord);
        }
        
        return logs;
    }
} 