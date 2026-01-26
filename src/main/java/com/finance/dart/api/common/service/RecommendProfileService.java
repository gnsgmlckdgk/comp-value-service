package com.finance.dart.api.common.service;

import com.finance.dart.api.common.dto.RecommendProfileDto;
import com.finance.dart.api.common.entity.RecommendProfileConfigEntity;
import com.finance.dart.api.common.entity.RecommendProfileEntity;
import com.finance.dart.api.common.repository.RecommendProfileConfigRepository;
import com.finance.dart.api.common.repository.RecommendProfileRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 추천종목 프로파일 서비스
 */
@Slf4j
@AllArgsConstructor
@Service
public class RecommendProfileService {

    private final RecommendProfileRepository profileRepository;
    private final RecommendProfileConfigRepository configRepository;

    /**
     * 프로파일 등록 (마스터 + 설정)
     */
    @Transactional
    public RecommendProfileDto regiProfile(RecommendProfileDto dto) {

        // 프로파일명 중복 체크
        Optional<RecommendProfileEntity> existingProfile = profileRepository.findByProfileName(dto.getProfileName());
        if (existingProfile.isPresent()) {
            throw new RuntimeException("이미 존재하는 프로파일명입니다: " + dto.getProfileName());
        }

        // 프로파일 마스터 저장
        RecommendProfileEntity profileEntity = new RecommendProfileEntity();
        profileEntity.setProfileName(dto.getProfileName());
        profileEntity.setProfileDesc(dto.getProfileDesc());
        profileEntity.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : "N");
        profileEntity.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);

        RecommendProfileEntity savedProfile = profileRepository.save(profileEntity);

        // 프로파일 설정 저장
        RecommendProfileConfigEntity configEntity = new RecommendProfileConfigEntity();
        configEntity.setProfile(savedProfile);
        setConfigFields(configEntity, dto);

        configRepository.save(configEntity);

        return toDto(savedProfile, configEntity);
    }

    /**
     * 프로파일 수정 (마스터 + 설정)
     */
    @Transactional
    public RecommendProfileDto modiProfile(RecommendProfileDto dto) {

        if (dto.getId() == null) {
            throw new RuntimeException("프로파일 ID가 필요합니다.");
        }

        // 프로파일 조회
        Optional<RecommendProfileEntity> profileOpt = profileRepository.findById(dto.getId());
        if (profileOpt.isEmpty()) {
            return null;
        }

        RecommendProfileEntity profileEntity = profileOpt.get();

        // 프로파일명 변경 시 중복 체크
        if (dto.getProfileName() != null && !dto.getProfileName().equals(profileEntity.getProfileName())) {
            Optional<RecommendProfileEntity> existingProfile = profileRepository.findByProfileName(dto.getProfileName());
            if (existingProfile.isPresent()) {
                throw new RuntimeException("이미 존재하는 프로파일명입니다: " + dto.getProfileName());
            }
            profileEntity.setProfileName(dto.getProfileName());
        }

        // 프로파일 마스터 수정
        if (dto.getProfileDesc() != null) profileEntity.setProfileDesc(dto.getProfileDesc());
        if (dto.getIsActive() != null) profileEntity.setIsActive(dto.getIsActive());
        if (dto.getSortOrder() != null) profileEntity.setSortOrder(dto.getSortOrder());
        profileEntity.setUpdatedAt(LocalDateTime.now());

        RecommendProfileEntity savedProfile = profileRepository.save(profileEntity);

        // 프로파일 설정 조회 및 수정
        Optional<RecommendProfileConfigEntity> configOpt = configRepository.findByProfile_Id(dto.getId());
        RecommendProfileConfigEntity configEntity;

        if (configOpt.isPresent()) {
            configEntity = configOpt.get();
            setConfigFields(configEntity, dto);
            configEntity.setUpdatedAt(LocalDateTime.now());
        } else {
            // 설정이 없으면 새로 생성
            configEntity = new RecommendProfileConfigEntity();
            configEntity.setProfile(savedProfile);
            setConfigFields(configEntity, dto);
        }

        configRepository.save(configEntity);

        return toDto(savedProfile, configEntity);
    }

    /**
     * 프로파일 삭제 (마스터 삭제 시 설정도 함께 삭제 - Cascade)
     */
    @Transactional
    public void delProfile(Long id) {
        if (id != null) {
            profileRepository.deleteById(id);
        }
    }

    /**
     * 프로파일 단건 조회
     */
    public RecommendProfileDto getProfile(Long id) {
        Optional<RecommendProfileEntity> profileOpt = profileRepository.findById(id);
        if (profileOpt.isEmpty()) {
            return null;
        }

        RecommendProfileEntity profileEntity = profileOpt.get();
        Optional<RecommendProfileConfigEntity> configOpt = configRepository.findByProfile_Id(id);

        return toDto(profileEntity, configOpt.orElse(null));
    }

    /**
     * 프로파일 목록 조회 (전체)
     */
    public List<RecommendProfileDto> getProfileList() {
        List<RecommendProfileEntity> profileList = profileRepository.findAllByOrderBySortOrder();

        return profileList.stream()
                .map(profile -> {
                    Optional<RecommendProfileConfigEntity> configOpt = configRepository.findByProfile_Id(profile.getId());
                    return toDto(profile, configOpt.orElse(null));
                })
                .collect(Collectors.toList());
    }

    /**
     * 활성화된 프로파일 목록 조회 (스케줄러에서 사용)
     */
    public List<RecommendProfileDto> getActiveProfileList() {
        List<RecommendProfileEntity> profileList = profileRepository.findByIsActiveOrderBySortOrder("Y");

        return profileList.stream()
                .map(profile -> {
                    Optional<RecommendProfileConfigEntity> configOpt = configRepository.findByProfile_Id(profile.getId());
                    return toDto(profile, configOpt.orElse(null));
                })
                .collect(Collectors.toList());
    }

    /**
     * 프로파일 활성화/비활성화 토글
     */
    @Transactional
    public RecommendProfileDto toggleActive(Long id) {
        Optional<RecommendProfileEntity> profileOpt = profileRepository.findById(id);
        if (profileOpt.isEmpty()) {
            return null;
        }

        RecommendProfileEntity profileEntity = profileOpt.get();
        profileEntity.setIsActive("Y".equals(profileEntity.getIsActive()) ? "N" : "Y");
        profileEntity.setUpdatedAt(LocalDateTime.now());

        RecommendProfileEntity savedProfile = profileRepository.save(profileEntity);
        Optional<RecommendProfileConfigEntity> configOpt = configRepository.findByProfile_Id(id);

        return toDto(savedProfile, configOpt.orElse(null));
    }

    /**
     * 설정 필드 세팅 헬퍼
     */
    private void setConfigFields(RecommendProfileConfigEntity configEntity, RecommendProfileDto dto) {
        // Stock Screener 조건
        if (dto.getMarketCapMin() != null) configEntity.setMarketCapMin(dto.getMarketCapMin());
        if (dto.getMarketCapMax() != null) configEntity.setMarketCapMax(dto.getMarketCapMax());
        if (dto.getBetaMax() != null) configEntity.setBetaMax(dto.getBetaMax());
        if (dto.getVolumeMin() != null) configEntity.setVolumeMin(dto.getVolumeMin());
        if (dto.getIsEtf() != null) configEntity.setIsEtf(dto.getIsEtf());
        if (dto.getIsFund() != null) configEntity.setIsFund(dto.getIsFund());
        if (dto.getIsActivelyTrading() != null) configEntity.setIsActivelyTrading(dto.getIsActivelyTrading());
        if (dto.getExchange() != null) configEntity.setExchange(dto.getExchange());
        if (dto.getScreenerLimit() != null) configEntity.setScreenerLimit(dto.getScreenerLimit());
        if (dto.getPriceMin() != null) configEntity.setPriceMin(dto.getPriceMin());
        if (dto.getPriceMax() != null) configEntity.setPriceMax(dto.getPriceMax());
        if (dto.getBetaMin() != null) configEntity.setBetaMin(dto.getBetaMin());
        if (dto.getVolumeMax() != null) configEntity.setVolumeMax(dto.getVolumeMax());
        if (dto.getSector() != null) configEntity.setSector(dto.getSector());
        if (dto.getIndustry() != null) configEntity.setIndustry(dto.getIndustry());
        if (dto.getCountry() != null) configEntity.setCountry(dto.getCountry());

        // 저평가 필터링 조건
        if (dto.getPeRatioMin() != null) configEntity.setPeRatioMin(dto.getPeRatioMin());
        if (dto.getPeRatioMax() != null) configEntity.setPeRatioMax(dto.getPeRatioMax());
        if (dto.getPbRatioMax() != null) configEntity.setPbRatioMax(dto.getPbRatioMax());
        if (dto.getRoeMin() != null) configEntity.setRoeMin(dto.getRoeMin());
        if (dto.getDebtEquityMax() != null) configEntity.setDebtEquityMax(dto.getDebtEquityMax());
    }

    /**
     * Entity -> DTO 변환
     */
    private RecommendProfileDto toDto(RecommendProfileEntity profile, RecommendProfileConfigEntity config) {
        RecommendProfileDto dto = new RecommendProfileDto();

        // 프로파일 마스터
        dto.setId(profile.getId());
        dto.setProfileName(profile.getProfileName());
        dto.setProfileDesc(profile.getProfileDesc());
        dto.setIsActive(profile.getIsActive());
        dto.setSortOrder(profile.getSortOrder());
        dto.setCreatedAt(profile.getCreatedAt() != null ? profile.getCreatedAt().toString() : null);
        dto.setUpdatedAt(profile.getUpdatedAt() != null ? profile.getUpdatedAt().toString() : null);

        // 설정
        if (config != null) {
            dto.setMarketCapMin(config.getMarketCapMin());
            dto.setMarketCapMax(config.getMarketCapMax());
            dto.setBetaMax(config.getBetaMax());
            dto.setVolumeMin(config.getVolumeMin());
            dto.setIsEtf(config.getIsEtf());
            dto.setIsFund(config.getIsFund());
            dto.setIsActivelyTrading(config.getIsActivelyTrading());
            dto.setExchange(config.getExchange());
            dto.setScreenerLimit(config.getScreenerLimit());
            dto.setPriceMin(config.getPriceMin());
            dto.setPriceMax(config.getPriceMax());
            dto.setBetaMin(config.getBetaMin());
            dto.setVolumeMax(config.getVolumeMax());
            dto.setSector(config.getSector());
            dto.setIndustry(config.getIndustry());
            dto.setCountry(config.getCountry());
            dto.setPeRatioMin(config.getPeRatioMin());
            dto.setPeRatioMax(config.getPeRatioMax());
            dto.setPbRatioMax(config.getPbRatioMax());
            dto.setRoeMin(config.getRoeMin());
            dto.setDebtEquityMax(config.getDebtEquityMax());
        }

        return dto;
    }
}
