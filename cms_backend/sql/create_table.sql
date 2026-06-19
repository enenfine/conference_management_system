DROP DATABASE IF EXISTS z_cms_system;
CREATE DATABASE z_cms_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE z_cms_system;

CREATE TABLE user (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  userAccount VARCHAR(256) NOT NULL COMMENT '账号',
  userPassword VARCHAR(512) NOT NULL COMMENT '密码',
  unionId VARCHAR(256) DEFAULT NULL,
  mpOpenId VARCHAR(256) DEFAULT NULL,
  userName VARCHAR(256) DEFAULT NULL COMMENT '用户昵称',
  userAvatar VARCHAR(1024) DEFAULT NULL COMMENT '用户头像',
  userProfile VARCHAR(512) DEFAULT NULL COMMENT '用户简介',
  userRole VARCHAR(256) NOT NULL DEFAULT 'user' COMMENT '用户角色：user/admin/ban',
  createTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updateTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  isDelete TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uniq_user_account (userAccount)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

CREATE TABLE meeting_type (
  id BIGINT NOT NULL AUTO_INCREMENT,
  typeName VARCHAR(32) NOT NULL COMMENT '类型名称',
  typeCode VARCHAR(32) NOT NULL COMMENT '类型编码',
  description VARCHAR(256) DEFAULT NULL COMMENT '类型描述',
  createTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updateTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  isDelete TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uniq_type_code (typeCode)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会议类型表';

CREATE TABLE meeting_room (
  id BIGINT NOT NULL AUTO_INCREMENT,
  roomName VARCHAR(256) NOT NULL COMMENT '会议室名称',
  roomCode VARCHAR(64) NOT NULL COMMENT '会议室编号',
  capacity INT NOT NULL COMMENT '容纳人数',
  location VARCHAR(512) NOT NULL COMMENT '位置',
  facilities VARCHAR(1024) DEFAULT NULL COMMENT '设施配置JSON数组',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-维护中，1-可用，2-已使用',
  createTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updateTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  isDelete TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uniq_room_code (roomCode)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会议室表';

CREATE TABLE meeting (
  id BIGINT NOT NULL AUTO_INCREMENT,
  title VARCHAR(256) NOT NULL COMMENT '会议标题',
  description TEXT DEFAULT NULL COMMENT '会议描述',
  meetingCode VARCHAR(64) NOT NULL COMMENT '会议编号',
  typeId BIGINT NOT NULL COMMENT '会议类型ID',
  roomId BIGINT NOT NULL COMMENT '会议室ID',
  startTime DATETIME NOT NULL COMMENT '开始时间',
  endTime DATETIME NOT NULL COMMENT '结束时间',
  status VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT '状态',
  creatorId BIGINT NOT NULL COMMENT '创建人ID',
  createTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updateTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  isDelete TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uniq_meeting_code (meetingCode),
  KEY idx_roomId (roomId),
  KEY idx_creatorId (creatorId),
  KEY idx_startTime (startTime),
  KEY idx_endTime (endTime)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会议表';

CREATE TABLE meeting_participant (
  id BIGINT NOT NULL AUTO_INCREMENT,
  meetingId BIGINT NOT NULL COMMENT '会议ID',
  userId BIGINT NOT NULL COMMENT '用户ID',
  role VARCHAR(32) NOT NULL DEFAULT 'ATTENDEE' COMMENT '角色：HOST/ATTENDEE',
  status VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/ACCEPTED/DECLINED',
  createTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updateTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  isDelete TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uniq_meeting_user (meetingId, userId),
  KEY idx_userId (userId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='参会人员表';

CREATE TABLE file_type (
  id BIGINT NOT NULL AUTO_INCREMENT,
  typeName VARCHAR(32) NOT NULL COMMENT '类型名称',
  typeCode VARCHAR(32) NOT NULL COMMENT '类型编码',
  mimeType VARCHAR(128) DEFAULT NULL COMMENT 'MIME类型',
  description VARCHAR(256) DEFAULT NULL COMMENT '说明',
  createTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updateTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  isDelete TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uniq_file_type_code (typeCode)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件类型表';

CREATE TABLE meeting_file (
  id BIGINT NOT NULL AUTO_INCREMENT,
  meetingId BIGINT NOT NULL COMMENT '会议ID',
  fileName VARCHAR(256) NOT NULL COMMENT '文件名',
  fileUrl VARCHAR(1024) NOT NULL COMMENT '文件地址',
  fileSize BIGINT NOT NULL COMMENT '文件大小',
  typeId BIGINT NOT NULL COMMENT '文件类型ID',
  uploaderId BIGINT NOT NULL COMMENT '上传者ID',
  createTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updateTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  isDelete TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_meetingId (meetingId),
  KEY idx_uploaderId (uploaderId),
  KEY idx_typeId (typeId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会议附件表';

INSERT INTO user (id, userAccount, userPassword, userName, userAvatar, userProfile, userRole) VALUES
(1, 'admin', 'dc094ae0edbd3ad3bbd36a9e46500e6b', '系统管理员', NULL, '系统管理员', 'admin'),
(2, 'user', 'dc094ae0edbd3ad3bbd36a9e46500e6b', '普通用户', NULL, '普通用户', 'user'),
(3, 'zhangsan', 'dc094ae0edbd3ad3bbd36a9e46500e6b', '张三', NULL, '技术部经理', 'user'),
(4, 'lisi', 'dc094ae0edbd3ad3bbd36a9e46500e6b', '李四', NULL, '市场部经理', 'user'),
(5, 'wangwu', 'dc094ae0edbd3ad3bbd36a9e46500e6b', '王五', NULL, '财务部主管', 'user');

INSERT INTO meeting_type (id, typeName, typeCode, description) VALUES
(1, '部门例会', 'DEPT_REGULAR', '部门定期会议'),
(2, '项目会议', 'PROJECT', '项目相关会议'),
(3, '培训会议', 'TRAINING', '培训相关会议'),
(4, '面试会议', 'INTERVIEW', '招聘面试会议'),
(5, '管理会议', 'MANAGEMENT', '管理层会议');

INSERT INTO meeting_room (id, roomName, roomCode, capacity, location, facilities, status) VALUES
(1, '主会议室', 'ROOM_301', 20, '3楼301', '["投影仪","白板","视频会议系统"]', 1),
(2, '小会议室A', 'ROOM_302', 8, '3楼302', '["白板","电视"]', 1),
(3, '培训室', 'ROOM_401', 30, '4楼401', '["投影仪","音响","白板"]', 1),
(4, '视频会议室', 'ROOM_402', 12, '4楼402', '["视频会议系统","电视","白板"]', 1);

INSERT INTO meeting (id, title, description, meetingCode, typeId, roomId, startTime, endTime, status, creatorId) VALUES
(1, '项目评审会', '新项目立项评审', 'MEETING_001', 2, 1, '2026-06-20 14:00:00', '2026-06-20 16:00:00', 'PENDING', 1),
(2, '员工培训', '新员工入职培训', 'MEETING_002', 3, 3, '2026-06-21 09:00:00', '2026-06-21 11:00:00', 'PENDING', 2),
(3, '部门预算会议', '讨论部门预算', 'MEETING_003', 5, 2, '2026-06-22 10:00:00', '2026-06-22 11:30:00', 'PENDING', 3);

INSERT INTO meeting_participant (meetingId, userId, role, status) VALUES
(1, 1, 'HOST', 'ACCEPTED'),
(1, 2, 'ATTENDEE', 'PENDING'),
(1, 3, 'ATTENDEE', 'PENDING'),
(2, 2, 'HOST', 'ACCEPTED'),
(2, 4, 'ATTENDEE', 'PENDING'),
(3, 3, 'HOST', 'ACCEPTED'),
(3, 5, 'ATTENDEE', 'PENDING');

INSERT INTO file_type (id, typeName, typeCode, mimeType, description) VALUES
(1, 'PDF文档', 'PDF', 'application/pdf', 'PDF格式文档'),
(2, 'Word文档', 'DOCX', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 'Word格式文档'),
(3, 'Excel表格', 'XLSX', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', 'Excel格式文档'),
(4, 'PowerPoint演示', 'PPTX', 'application/vnd.openxmlformats-officedocument.presentationml.presentation', 'PPT格式文档'),
(5, '压缩文件', 'ZIP', 'application/zip', '压缩文件'),
(6, '文本文件', 'TXT', 'text/plain', '文本文件');
