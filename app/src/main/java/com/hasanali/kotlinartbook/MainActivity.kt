package com.hasanali.kotlinartbook

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.hasanali.kotlinartbook.databinding.ActivityMainBinding
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var artList: ArrayList<Art>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val v = binding.root
        setContentView(v)

        artList = ArrayList()
        val artAdapter = ArtBookAdapter(artList)
        binding.recyclerView.adapter = artAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        try {
            val myDb = this.openOrCreateDatabase("Arts", MODE_PRIVATE, null)
            val cursor = myDb.rawQuery("SELECT * FROM arts", null)
            val idIx = cursor.getColumnIndex("id")
            val artnameIx = cursor.getColumnIndex("artname")
            while (cursor.moveToNext()) {
                val art = Art(cursor.getString(artnameIx), cursor.getInt(idIx))
                artList.add(art)
            }
            cursor.close()
            artAdapter.notifyDataSetChanged()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.art_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(R.id.add_art_item == item.itemId) {
            val intent = Intent(this, ArtActivity::class.java)
            intent.putExtra("info", "new")
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }
}