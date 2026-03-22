package com.example.codereminder.repository;

import com.example.codereminder.domain.ReviewItem;
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
public class JdbcRepository {

    private final DataSource dataSource;

    public void save(ReviewItem reviewItem) {
        String sql = "insert into submission(id, user_name, problem_id, result_text, timestamp, last_attempt_timestamp) values(?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, reviewItem.getId());
            pstmt.setString(2, reviewItem.getUserName());
            pstmt.setLong(3, reviewItem.getProblemId());
            pstmt.setString(4, reviewItem.getResultText());
            pstmt.setLong(5, reviewItem.getTimestamp());
            pstmt.setLong(6, reviewItem.getTimestamp());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("저장 중 오류 발생", e);
        } finally {
            close(conn, pstmt);
        }
    }

    public void remove(String id) {
        String sql = "delete from submission where id = ?";

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

    /*public Optional<ReviewItem> findByUserNameAndProblemId(@NotBlank String userName, @NotNull Long problemId) {
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
                ReviewItem reviewItem = ReviewItem.of(rs.getString("id"),
                        rs.getString("user_name"),
                        rs.getLong("problem_id"),
                        rs.getString("result_text"),
                        rs.getLong("timestamp"),
                        rs.getLong("last_attempt_timestamp"));

                return Optional.of(reviewItem);
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            close(conn, pstmt, rs);
        }
    }
*/
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
        } finally {
            close(conn, pstmt);
        }
    }

    /*public Optional<ReviewItems> findByUserName(String userName) {
        LocalDate now = LocalDate.now();
        String sql = "select * from submission where user_name = ? and  next_review_date = now";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs;

        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, userName);

            rs = pstmt.executeQuery();

            if(rs.next()){
                rs.getString()

                Optional.of(ReviewItems);
            }

            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }finally {
            close(conn, pstmt, rs);
        }
    }*/

    private void close(Connection conn, PreparedStatement pstmt) {
        if (pstmt != null) {
            try {
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
            try {
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
                throw new RuntimeException("ResultSet 닫기는 도중 에러남", e);
            }
        }
    }
}
