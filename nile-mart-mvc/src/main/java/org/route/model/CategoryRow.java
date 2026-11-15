package org.route.model;

/** One line of the categories table. */
public record CategoryRow(String displayName, String slug, String parentName,
                          int displayOrder, boolean active) {
}
