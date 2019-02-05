package com.mirrorfly.gateway.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mirrorfly.gateway.model.FileUploads;

@Repository
public interface FileUploadRepo extends JpaRepository<FileUploads, Long> {

}
