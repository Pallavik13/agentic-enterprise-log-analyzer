package com.pallavi.loganalyzer.log;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogFileRepository extends JpaRepository<LogFile, Long> {
}
