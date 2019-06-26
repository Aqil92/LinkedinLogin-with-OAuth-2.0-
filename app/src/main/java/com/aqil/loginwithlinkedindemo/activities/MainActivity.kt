package com.aqil.loginwithlinkedindemo.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.aqil.loginwithlinkedindemo.R
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.incipientinfo.loginwithlinkedin.Linkedin
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject

class MainActivity : AppCompatActivity(), Linkedin.onLinkedinResponce {

     val APIKEY = "APIKEY"
     val SECRETKEY = "SECRETKEY"
     val REDIRECTURI = "REDIRECTURI"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        try {
            Linkedin.init(this, APIKEY, SECRETKEY, REDIRECTURI)

            btnLogin.setOnClickListener {

                Linkedin.login()

            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onSuccess(responce: JSONObject?) {

        try {

            val fName = responce!!.getString("firstName")
            val lName = responce.getString("lastName")
            val userEmail = responce.getString("userEmail")
            val profileImage = responce.getString("profileImage")

            Glide.with(this).load(profileImage).apply(RequestOptions.circleCropTransform()).into(imgProfile)

            txtTitle.visibility = View.VISIBLE

            tvDetail.text = "$fName $lName\n$userEmail"

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

}
