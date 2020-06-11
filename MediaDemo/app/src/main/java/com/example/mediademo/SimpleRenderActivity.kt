package com.example.mediademo

import android.graphics.BitmapFactory
import com.example.mediademo.databinding.ActivitySimpleRenderBinding
import com.example.mediademo.opengl.BitmapDrawer
import com.example.mediademo.opengl.IDrawer
import com.example.mediademo.opengl.SimpleRender
import com.example.mediademo.opengl.TriangleDrawer
import com.example.mediademo.ui.kotlin.KTBaseUIActivity
import kotlinx.android.synthetic.main.activity_simple_render.*

class SimpleRenderActivity : KTBaseUIActivity<ActivitySimpleRenderBinding>() {

    private lateinit var drawer: IDrawer

    override fun getLayoutId(): Int {
        return R.layout.activity_simple_render
    }

    override fun init() {

    }

    override fun initEvent() {
    }

    override fun initData() {
        drawer = if (intent.getIntExtra("type", 0) == 0) {
            TriangleDrawer()
        } else {
            BitmapDrawer(BitmapFactory.decodeResource(resources, R.drawable.cover))
        }
        initRender(drawer)
    }

    private fun initRender(drawer: IDrawer) {
        gl_surface.setEGLContextClientVersion(2)
        val render = SimpleRender()
        render.addDrawer(drawer)
        gl_surface.setRenderer(render)
    }

    override fun onDestroy() {
        drawer.release()
        super.onDestroy()
    }
}
