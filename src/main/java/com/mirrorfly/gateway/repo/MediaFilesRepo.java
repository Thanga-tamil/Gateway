package com.mirrorfly.gateway.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mirrorfly.gateway.model.MediaFiles;

@Repository
public interface MediaFilesRepo extends JpaRepository<MediaFiles, Long> {

	public MediaFiles findByFileToken(@Param("fileToken") String fileToken);

}
