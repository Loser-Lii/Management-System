package com.example.backend.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class PerformanceViewRepositoryImpl implements PerformanceViewRepository {

    private final JdbcTemplate jdbcTemplate;

    public PerformanceViewRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final String BASE_SQL = "SELECT " +
            "salesman_id, salesman_name, total_sales, total_commission, sales_count, avg_satisfaction, " +
            "attitude_complaints, attitude_critical, attitude_high, attitude_normal, attitude_low, attitude_penalty " +
            "FROM vw_salesman_performance";

    private static final RowMapper<PerformanceViewRow> ROW_MAPPER = new RowMapper<>() {
        @Override
        public PerformanceViewRow mapRow(ResultSet rs, int rowNum) throws SQLException {
            PerformanceViewRow row = new PerformanceViewRow();
            row.setSalesmanId(rs.getLong("salesman_id"));
            row.setSalesmanName(rs.getString("salesman_name"));
            row.setTotalSales(rs.getBigDecimal("total_sales"));
            row.setTotalCommission(rs.getBigDecimal("total_commission"));
            row.setSalesCount(rs.getLong("sales_count"));
            double avg = rs.getDouble("avg_satisfaction");
            row.setAverageSatisfaction(rs.wasNull() ? null : avg);
            row.setAttitudeComplaintCount(rs.getLong("attitude_complaints"));
            row.setAttitudeCriticalCount(rs.getLong("attitude_critical"));
            row.setAttitudeHighCount(rs.getLong("attitude_high"));
            row.setAttitudeNormalCount(rs.getLong("attitude_normal"));
            row.setAttitudeLowCount(rs.getLong("attitude_low"));
            row.setAttitudePenalty(rs.getBigDecimal("attitude_penalty"));
            return row;
        }
    };

    @Override
    public List<PerformanceViewRow> findAllView() {
        return jdbcTemplate.query(BASE_SQL, ROW_MAPPER);
    }

    @Override
    public PerformanceViewRow findBySalesmanId(Long salesmanId) {
        String sql = BASE_SQL + " WHERE salesman_id = ?";
        List<PerformanceViewRow> list = jdbcTemplate.query(sql, ROW_MAPPER, salesmanId);
        return list.isEmpty() ? null : list.get(0);
    }
}
