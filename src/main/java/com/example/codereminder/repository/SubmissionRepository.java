package com.example.codereminder.repository;

import com.example.codereminder.domain.Submission;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

@Repository
@RequiredArgsConstructor
public class SubmissionRepository {

    private final DataSource dataSource;

    public void save(Submission submission) {
        String sql = "insert into submission(id, user_id, problem_id, result_text, timestamp, last_attempt_date) values(?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, submission.getId());
            pstmt.setString(2, submission.getUserId());
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


    public Optional<Submission> findByUserIdAndProblemId(@NotBlank String userId, @NotNull Long problemId) {
        String sql = "select * from submission where user_id = ? and problem_id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userId);
            pstmt.setLong(2, problemId);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                Submission submission = Submission.of(rs.getString("id"), rs.getString("user_id"), rs.getLong("problem_id"), rs.getString("result_text"), rs.getLong("timestamp"), rs.getLong("last_attempt_date"));
                return Optional.of(submission);
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }finally {
            close(conn, pstmt, rs);
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
}
