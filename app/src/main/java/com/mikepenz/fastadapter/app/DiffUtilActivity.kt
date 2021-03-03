package com.mikepenz.fastadapter.app

import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.mikepenz.fastadapter.adapters.FastItemAdapter
import com.mikepenz.fastadapter.app.databinding.ActivitySampleBinding
import com.mikepenz.fastadapter.app.items.SimpleItem
import com.mikepenz.fastadapter.diff.DiffCallback
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.materialdesigniconic.MaterialDesignIconic
import com.mikepenz.iconics.utils.actionBar
import com.mikepenz.iconics.utils.colorInt
import com.mikepenz.itemanimators.AlphaInAnimator
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.*
import kotlin.random.Random

/**
 * Created by Aleksander Mielczarek on 07.08.2017.
 */

class DiffUtilActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySampleBinding

    //save our FastAdapter
    private lateinit var fastItemAdapter: FastItemAdapter<SimpleItem>
    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySampleBinding.inflate(layoutInflater).also {
            setContentView(it.root)
        }

        // Handle Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setTitle(R.string.sample_diff_util)

        //create our FastAdapter which will manage everything
        fastItemAdapter = FastItemAdapter()

        //get our recyclerView and do basic setup
        binding.rv.layoutManager = LinearLayoutManager(this)
        binding.rv.itemAnimator = AlphaInAnimator()
        binding.rv.adapter = fastItemAdapter

        //fill with some sample data
        setDataAsync()

        //set the back arrow in the toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(false)

        //restore selections (this has to be done after the items were added
        fastItemAdapter.withSavedInstanceState(savedInstanceState)
    }

    override fun onSaveInstanceState(_outState: Bundle) {
        var outState = _outState
        //add the values which need to be saved from the adapter to the bundel
        outState = fastItemAdapter.saveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.refresh, menu)
        menu.findItem(R.id.item_refresh).icon = IconicsDrawable(this, MaterialDesignIconic.Icon.gmi_refresh).apply { colorInt = Color.BLACK; actionBar() }
        menu.findItem(R.id.item_refresh_async).icon = IconicsDrawable(this, MaterialDesignIconic.Icon.gmi_refresh_sync).apply { colorInt = Color.BLACK; actionBar() }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //handle the click on the back arrow click
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            R.id.item_refresh -> {
                Toast.makeText(this, "Refresh synchronous", Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.item_refresh_async -> {
                Toast.makeText(this, "Refresh asynchronous", Toast.LENGTH_SHORT).show()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }

    private fun setDataAsync() {
        MainScope().launch {
            while (true) {
                delay(100)
                val data = createData()

                try {
                    withContext(Dispatchers.Default) {
                        val result =
                            FastAdapterDiffUtil.calculateDiff(fastItemAdapter.itemAdapter, data)
                        withContext(Dispatchers.Main) {
                            FastAdapterDiffUtil[fastItemAdapter.itemAdapter] = result
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun createData(): List<SimpleItem> {
        val items = mutableListOf<SimpleItem>()
        repeat(Random.nextInt(100)) {
            items.add(SimpleItem().withName("Item ${it + 1}").withIdentifier((it + 1).toLong()))
        }
        return items
    }
}
