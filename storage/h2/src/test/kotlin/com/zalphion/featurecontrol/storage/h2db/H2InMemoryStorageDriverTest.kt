package com.zalphion.featurecontrol.storage.h2db

import com.zalphion.featurecontrol.storage.StorageDriver
import com.zalphion.featurecontrol.storage.StorageDriverContract

class H2InMemoryStorageDriverTest: StorageDriverContract(StorageDriver::h2DbInMemory)