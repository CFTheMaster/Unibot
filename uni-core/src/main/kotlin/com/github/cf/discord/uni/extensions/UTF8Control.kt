package com.github.cf.discord.uni.extensions

import java.io.InputStream
import java.io.InputStreamReader
import java.util.*

// https://stackoverflow.com/questions/4659929/how-to-use-utf-8-in-resource-properties-with-resourcebundle

class UTF8Control : ResourceBundle.Control() {
    override fun newBundle(p0: String, p1: Locale, p2: String, p3: ClassLoader, p4: Boolean): ResourceBundle {
        val bundleName = toBundleName(p0, p1)
        val resourceName = toResourceName(bundleName, "properties")
        var bundle: ResourceBundle? = null
        var stream: InputStream? = null

        if (p4) {
            val url = p3.getResource(resourceName)
            if (url != null) {
                val connection = url.openConnection()
                if (connection != null) {
                    connection.useCaches = false
                    stream = connection.getInputStream()
                }
            }
        } else {
            stream = p3.getResourceAsStream(resourceName)
        }

        if (stream != null) {
            try {
                bundle = PropertyResourceBundle(InputStreamReader(stream, "UTF-8"))
            } finally {
                stream.close()
            }
        }

        return bundle as ResourceBundle
    }
}