package io.github.upinmcse.writer.repository;

import io.github.upinmcse.common.model.Item;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

@Repository
public class ItemRepository {

    private final JdbcTemplate jdbc;

    public ItemRepository(JdbcTemplate jdbc){
        this.jdbc = jdbc;
    }

    private static final RowMapper<Item> ROW_MAPPER = (rs, rn) -> new Item(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getLong("price_cents"),
            rs.getString("currency"),
            (Integer) rs.getObject("stock"),
            toInstant(rs.getTimestamp("created_at")),
            toInstant(rs.getTimestamp("updated_at"))
    );

    @Transactional
    public Item insert(Item item) {
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO items (" +
                            "name, " +
                            "description, " +
                            "price_cents, " +
                            "currency, " +
                            "stock) "
                            + "VALUES (?, ?, ?, ?, ?)",
                    new String[]{"id"});
            ps.setString(1, item.name());
            ps.setString(2, item.description());
            ps.setLong(3, item.priceCents());
            ps.setString(4, item.currency());
            if (item.stock() == null) {
                ps.setNull(5, java.sql.Types.INTEGER);
            } else {
                ps.setInt(5, item.stock());
            }
            return ps;
        }, keys);
        long id = Objects.requireNonNull(keys.getKey()).longValue();
        return findById(id).orElseThrow(() ->
                new IllegalStateException("freshly inserted row " + id + " not visible"));
    }

    public Optional<Item> findById(long id) {
        try {
            return Optional.ofNullable(jdbc.queryForObject(
                    "SELECT id, name, description, price_cents, currency, stock, created_at, updated_at "
                            + "FROM items WHERE id = ?",
                    ROW_MAPPER, id));
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    /** Returns true if a row was actually updated. */
    @Transactional
    public boolean update(long id, Item patchItem) {
        int n = jdbc.update(
                "UPDATE items SET name = ?, description = ?, price_cents = ?, currency = ?, stock = ? "
                        + "WHERE id = ?",
                patchItem.name(),
                patchItem.description(),
                patchItem.priceCents(),
                patchItem.currency(),
                patchItem.stock(),
                id
        );
        return n > 0;
    }

    @Transactional
    public boolean delete(long id) {
        return jdbc.update("DELETE FROM items WHERE id = ?", id) > 0;
    }

    private static Instant toInstant(Timestamp ts) {
        return ts == null ? null : ts.toInstant();
    }
}
