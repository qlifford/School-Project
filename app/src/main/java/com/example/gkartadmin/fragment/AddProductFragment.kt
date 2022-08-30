package com.example.gkartadmin.fragment

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.gkartadmin.R
import com.example.gkartadmin.adapter.AddProductImageAdapter
import com.example.gkartadmin.databinding.FragmentAddProductBinding
import com.example.gkartadmin.model.AddProductModel
import com.example.gkartadmin.model.CategoryModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.util.*


class AddProductFragment : AppCompatActivity() {
    private lateinit var binding: FragmentAddProductBinding
    private lateinit var list: ArrayList<Uri>
    private lateinit var listImages: ArrayList<String>
    private lateinit var adapter: AddProductImageAdapter
    private var coverImage: Uri? = null
    private lateinit var dialog: Dialog
    private var coverImageUrl: String? = ""
    private lateinit var categoryList: ArrayList<String>

    private var launchGalleryActivity = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            coverImage = it.data!!.data
            binding.productCoverImage.setImageURI(coverImage)

            binding.productCoverImage.visibility = View.VISIBLE
        }
    }

    private var launchProductActivity = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            val imageUrl = it.data!!.data
            list.add(imageUrl!!)
            adapter.notifyDataSetChanged()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)
        list = ArrayList()
        listImages = ArrayList()

        dialog = Dialog(this)
        dialog.setContentView(R.layout.progress_layout)
        dialog.setCancelable(false)

        binding.selectCoverImage.setOnClickListener {
            val intent = Intent("android.intent.action.GET_CONTENT")
            intent.type = "image/*"
            launchGalleryActivity.launch(intent)
        }

        binding.productImageBtn.setOnClickListener {
            val intent = Intent("android.intent.action.GET_CONTENT")
            intent.type = "image/*"
            launchProductActivity.launch(intent)
        }

        setProductCategory()

        adapter = AddProductImageAdapter(list)
        binding.productImageRecyclerView.adapter = adapter

        binding.submitProductBtn.setOnClickListener {
            validateData()
        }

    }

    private fun validateData() {
        if (binding.edtProductName.text.toString().isEmpty()) {
            binding.edtProductName.requestFocus()
            binding.edtProductName.error = "Empty"

        } else if (binding.edtProductSp.text.toString().isEmpty()) {
            binding.edtProductSp.requestFocus()
            binding.edtProductSp.error = "Empty"

        } else if (coverImage == null) {
            Toast.makeText(this, "Please select cover image", Toast.LENGTH_SHORT).show()
        } else if (list.size < 1) {
            Toast.makeText(this, "Please select product images", Toast.LENGTH_SHORT)
                .show()
        } else {
            uploadImage()
        }
    }

    private fun uploadImage() {
        dialog.show()

        val fileName = UUID.randomUUID().toString() + ".jpg"

        val refStorage = FirebaseStorage.getInstance().reference.child("products/$fileName")
        refStorage.putFile(coverImage!!)
            .addOnSuccessListener {
                it.storage.downloadUrl.addOnSuccessListener { image ->
                    coverImageUrl = image.toString()

                    uploadProductImage()
                }
            }
            .addOnFailureListener {
                dialog.dismiss()
                Toast.makeText(
                    this, "Something went wrong with storage",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
    }

    private var i = 0
    private fun uploadProductImage() {
        dialog.show()

        val fileName = UUID.randomUUID().toString() + ".jpg"

        val refStorage = FirebaseStorage.getInstance().reference.child("products/$fileName")
        refStorage.putFile(list[i])
            .addOnSuccessListener {
                it.storage.downloadUrl.addOnSuccessListener { image ->
                    listImages.add(image!!.toString())
                    if (list.size == listImages.size) {
                        storeData()
                    } else {
                        i += 1
                        uploadProductImage()
                    }
                }
                    .addOnFailureListener {
                        dialog.dismiss()
                        Toast.makeText(
                            this, "Something went wrong with storage",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
            }
    }

    private fun storeData() {
        val db = Firebase.firestore.collection("products")
        val key = db.document().id

        val data = AddProductModel(
            binding.edtProductName.text.toString(),
            binding.edtProductDesc.text.toString(),
            coverImageUrl.toString(),
            categoryList[binding.productCategoryDropdown.selectedItemPosition],
            key,
            binding.edtProductMrp.text.toString(),
            binding.edtProductSp.text.toString(),
            listImages
        )
        db.document(key).set(data).addOnSuccessListener {
            dialog.dismiss()
            Toast.makeText(this, "Product Added", Toast.LENGTH_SHORT).show()
            binding.edtProductName.text = null
        }
            .addOnFailureListener {
                dialog.dismiss()
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()

            }
    }

    private fun setProductCategory() {
        categoryList = ArrayList()
        Firebase.firestore.collection("categories").get().addOnSuccessListener {
            categoryList.clear()
            for (doc in it.documents) {
                val data = doc.toObject(CategoryModel::class.java)
                categoryList.add(data!!.cat!!)
            }
            categoryList.add(0, "Select Category")
            val arrayAdapter = ArrayAdapter(this, R.layout.dropdown_item, categoryList)
            binding.productCategoryDropdown.adapter = arrayAdapter
        }

    }
}