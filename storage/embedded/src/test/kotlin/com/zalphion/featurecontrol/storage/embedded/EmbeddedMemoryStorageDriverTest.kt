package com.zalphion.featurecontrol.storage.embedded

import com.zalphion.featurecontrol.storage.StorageDriver
import com.zalphion.featurecontrol.storage.StorageDriverContract

class EmbeddedMemoryStorageDriverTest: StorageDriverContract(StorageDriver::embeddedMemory)