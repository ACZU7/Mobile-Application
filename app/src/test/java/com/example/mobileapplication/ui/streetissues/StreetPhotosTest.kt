package com.example.mobileapplication.ui.streetissues

import com.example.mobileapplication.data.model.StreetCategory
import com.example.mobileapplication.data.repository.StreetReportRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class StreetPhotosTest {
    private val repo = StreetReportRepository({ LocalDate.of(2026, 11, 9) }, seedSamples = false)
    private val vm = StreetIssuesViewModel(repo)

    private fun fillValidForm() {
        vm.category(StreetCategory.POTHOLE)
        vm.description("A fictional pothole by the crossing.")
    }

    @Test fun photoIsOptional() {
        fillValidForm()
        assertTrue(vm.submit())
        assertTrue(repo.reports.value.single().imageUris.isEmpty())
    }

    @Test fun addedPhotosAreSavedWithTheReportInOrder() {
        fillValidForm()
        vm.addPhoto("file:///photo1.jpg")
        vm.addPhoto("file:///photo2.jpg")
        assertTrue(vm.submit())
        assertEquals(listOf("file:///photo1.jpg", "file:///photo2.jpg"), repo.reports.value.single().imageUris)
    }

    @Test fun formIsClearedOfPhotosAfterSubmit() {
        fillValidForm()
        vm.addPhoto("file:///photo1.jpg")
        vm.submit()
        assertTrue(vm.formState.value.photos.isEmpty())
    }

    @Test fun photoCanBeRemovedBeforeSubmit() {
        vm.addPhoto("file:///photo1.jpg")
        vm.addPhoto("file:///photo2.jpg")
        vm.removePhoto("file:///photo1.jpg")
        assertEquals(listOf("file:///photo2.jpg"), vm.formState.value.photos)
    }

    @Test fun atMostThreePhotos() {
        (1..4).forEach { vm.addPhoto("file:///photo$it.jpg") }
        assertEquals(STREET_MAX_PHOTOS, vm.formState.value.photos.size)
        assertFalse(vm.formState.value.canAddPhoto)
        vm.removePhoto("file:///photo1.jpg")
        assertTrue(vm.formState.value.canAddPhoto)
    }

    @Test fun samePhotoIsNotAddedTwice() {
        vm.addPhoto("file:///photo1.jpg")
        vm.addPhoto("file:///photo1.jpg")
        assertEquals(1, vm.formState.value.photos.size)
    }

    @Test fun photosDoNotAffectValidation() {
        vm.addPhoto("file:///photo1.jpg")
        assertFalse(vm.submit())
        assertEquals(listOf("file:///photo1.jpg"), vm.formState.value.photos)
    }

    @Test fun cameraCategoryChoiceUpdatesTheForm() {
        vm.category(StreetCategory.GRAFFITI)
        assertEquals(StreetCategory.GRAFFITI, vm.formState.value.category)
    }
}
