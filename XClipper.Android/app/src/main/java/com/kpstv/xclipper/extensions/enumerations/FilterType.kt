package com.kpstv.xclipper.extensions.enumerations

/**
 * Specifies the filter type as parameter to work with the repository
 * update feature.
 */
enum class FilterType {
    /**
     * Make a direct with the dao.
     */
    Id,

    /**
     * When used then it will compare the data using filter and only
     * update data, time by querying id as filter.
     */
    Text,
}