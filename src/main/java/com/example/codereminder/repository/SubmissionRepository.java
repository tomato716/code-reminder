package com.example.codereminder.repository;

import com.example.codereminder.domain.Submission;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SubmissionRepository {

    private final DataSource dataSource;

    public void save(Submission submission) {
        String sql = "insert into submission(id, user_id, problem_id, result_text, timestamp, last_attempt_timestamp) values(?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, submission.getId());
            pstmt.setString(2, submission.getUserName());
            pstmt.setLong(3, submission.getProblemId());
            pstmt.setString(4, submission.getResultText());
            pstmt.setLong(5, submission.getTimestamp());
            pstmt.setLong(6, submission.getTimestamp());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("저장 중 오류 발생", e);
        }finally {
            close(conn, pstmt);
        }
    }

    public void remove(String id) {
        String sql ="delete from submission where id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("DB에서 제거 중 오류 발생", e);
        } finally {
            close(conn, pstmt);
        }
    }

    public Optional<Submission> findByUserNameAndProblemId(@NotBlank String userName, @NotNull Long problemId) {
        String sql = "select * from submission where user_name = ? and problem_id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userName);
            pstmt.setLong(2, problemId);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                Submission submission = Submission.of(rs.getString("id"),
                        rs.getString("user_id"),
                        rs.getLong("problem_id"),
                        rs.getString("result_text"),
                        rs.getLong("timestamp"),
                        rs.getLong("last_attempt_timestamp"));

                return Optional.of(submission);
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }finally {
            close(conn, pstmt, rs);
        }
    }

    public void updateLastAttemptTimestamp(String id, Long timestamp) {
        String sql = "update submission set last_attempt_timestamp = ? where  id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, timestamp);
            pstmt.setString(2, id);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }finally {
            close(conn,pstmt);
        }
    }

    private void close(Connection conn, PreparedStatement pstmt) {
        if (pstmt != null) {
            try{
                pstmt.close();
            } catch (SQLException e) {
                throw new RuntimeException("pstmt 닫기는 도중 에러남", e);
            }
        }

        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                throw new RuntimeException("conn 닫기는 도중 에러남", e);
            }
        }
    }

    private void close(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        if (pstmt != null) {
            try{
                pstmt.close();
            } catch (SQLException e) {
                throw new RuntimeException("PreparedStatement 닫기는 도중 에러남", e);
            }
        }

        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                throw new RuntimeException("Connection 닫기는 도중 에러남", e);
            }
        }

        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                throw new RuntimeException("ResultSet 닫기는 도중 에러남",e);
            }
        }
    }
}
