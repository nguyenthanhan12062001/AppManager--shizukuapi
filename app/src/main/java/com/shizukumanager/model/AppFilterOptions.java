package com.shizukumanager.app.model;

// Tuy chon tim kiem, loc va sap xep danh sach ung dung
public class AppFilterOptions {

    public enum TypeFilter {
        ALL,
        USER_ONLY,
        SYSTEM_ONLY
    }

    public enum StateFilter {
        ALL,
        ENABLED,
        DISABLED,
        SUSPENDED
    }

    public enum SortOrder {
        NAME_ASC,
        NAME_DESC,
        UPDATED_DESC
    }

    private final String searchQuery;
    private final TypeFilter typeFilter;
    private final StateFilter stateFilter;
    private final SortOrder sortOrder;

    public AppFilterOptions(
            String searchQuery,
            TypeFilter typeFilter,
            StateFilter stateFilter,
            SortOrder sortOrder
    ) {
        this.searchQuery = searchQuery == null ? "" : searchQuery;
        this.typeFilter = typeFilter == null ? TypeFilter.ALL : typeFilter;
        this.stateFilter = stateFilter == null ? StateFilter.ALL : stateFilter;
        this.sortOrder = sortOrder == null ? SortOrder.NAME_ASC : sortOrder;
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public TypeFilter getTypeFilter() {
        return typeFilter;
    }

    public StateFilter getStateFilter() {
        return stateFilter;
    }

    public SortOrder getSortOrder() {
        return sortOrder;
    }
}
