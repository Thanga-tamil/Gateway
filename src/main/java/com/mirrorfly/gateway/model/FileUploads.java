package com.mirrorfly.gateway.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "chat_media_files")
@Data
public class FileUploads implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6576324953564602096L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "file_id")
	private String fileId;

	@Column(name = "from_user")
	private String fromUser;

	@Column(name = "to_user")
	private String toUser;

	@Column(name = "group_id")
	private String groupId;

	@Column(name = "mime_type")
	private String mimeType;

	@Column(name = "file_name")
	private String fileName;

	@Column(name = "file_path")
	private String filePath;

	@Column(name = "is_server")
	private Integer isServer;

	@Column(name = "is_profile")
	private Integer isProfile;

	@Column(name = "from_is_deleted")
	private Integer fromIsDeleted;

	@Column(name = "to_is_deleted")
	private Integer toIsDeleted;

	@Column(name = "group_is_deleted")
	private Integer groupIsDeleted;

	@Column(name = "is_active")
	private Integer isActive;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

}
