package com.kpstv.xclipper.extensions.listeners

/**
 * Use to determine if any data exist in repository. Mainly used
 * with check functions which determine the availability of duplicate data.
 *
 * @param dataExist Receives callback when item exist
 * @param notFound Receives callback when not found
 */
class RepositoryListener (
    private val dataExist: () -> Unit,
    private val notFound: () -> Unit
): RepositoryFunctions {
    override fun onDataExist() {
        dataExist.invoke()
    }

    override fun onDataError() {
        notFound.invoke()
    }

}

private interface RepositoryFunctions {
    fun onDataExist()
    fun onDataError()
}