package com.studyhaja.service;

import com.studyhaja.domain.Zone;
import com.studyhaja.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

@Transactional
@Service
@RequiredArgsConstructor
public class ZoneService {

    private final ZoneRepository zoneRepository;

    // ZoneService 객체가 Bean 생성 및 등록된 후에 실행되는 어노테이션
    @PostConstruct
    public void initZoneData() throws IOException {

        if (zoneRepository.count() == 0) {
            Resource resource = new ClassPathResource("kor_city_list.csv");
            List<Zone> zones = Files.readAllLines(resource.getFile().toPath(), StandardCharsets.UTF_8).stream()
                    .map(line -> {
                        String[] split = line.split(",");
                        return Zone.builder().city(split[0]).localName0fCity(split[1]).province(split[2]).build();
                    }).collect(Collectors.toList());
            zoneRepository.saveAll(zones);
        }
    }
}