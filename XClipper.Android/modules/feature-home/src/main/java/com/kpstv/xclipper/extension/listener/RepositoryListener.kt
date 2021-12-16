package com.kpstv.xclipper.extension.listener

import com.kpstv.xclipper.extensions.SimpleFunction

/**
 * Use to determine if any data exist in repository. Mainly used
 * with check functions which determine the availability of duplicate data.
 *
 * @param dataExist Receives callback when item exist
 * @param notFound Receives callback when not found
 */
class RepositoryListener (
    private val dataExist: SimpleFunction,
    private val notFound: SimpleFunction
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