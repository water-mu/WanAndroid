package com.leshu.superbrain.ui.homeplaza

import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnLoadMoreListener
import com.coder.zzq.smartshow.toast.SmartToast
import com.leshu.superbrain.R
import com.leshu.superbrain.adapter.HomePageAdapter
import com.leshu.superbrain.ui.base.BaseVMFragment
import com.leshu.superbrain.view.loadpage.BasePageStateView
import com.leshu.superbrain.view.loadpage.LoadPageView
import com.leshu.superbrain.view.loadpage.SimpleLoadPageView
import com.leshu.superbrain.vm.HomePlazaViewModel
import kotlinx.android.synthetic.main.fragment_recycleview.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.koin.androidx.viewmodel.ext.android.getViewModel

/**
 *@创建者wwy
 *@创建时间 2020/6/1 11:35
 *@描述
 */
class HomePlazaFragment : BaseVMFragment<HomePlazaViewModel>(), OnLoadMoreListener {
    private val homePageAdapter = HomePageAdapter()
    private val loadPageView: BasePageStateView = SimpleLoadPageView()
    private lateinit var rootView: LoadPageView
    override fun initVM(): HomePlazaViewModel = getViewModel()

    override fun setLayoutResId(): Int = R.layout.fragment_recycleview

    override fun initView() {
        rootView = activity?.let { activity -> loadPageView.getRootView(activity) } as LoadPageView
        rootView.apply {
            failTextView().onClick { refresh() }
            noNetTextView().onClick { refresh() }
        }

        ArticleRv.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = homePageAdapter
        }
        homePageAdapter.apply {
            loadMoreModule.setOnLoadMoreListener(this@HomePlazaFragment)
            isAnimationFirstOnly = true
            setAnimationWithDefault(BaseQuickAdapter.AnimationType.ScaleIn)
        }
        refreshLayout.setOnRefreshListener { refresh() }
        refreshLayout.setEnableLoadMore(false)
    }

    private fun refresh() {
        mViewModel.loadSquareArticleList(true)
    }

    override fun initData() {
        refresh()
    }

    override fun startObserve() {
        mViewModel.apply {
            mHomePlazaListModel.observe(this@HomePlazaFragment, Observer {
                if (it.isRefresh) refreshLayout.finishRefresh(it.isRefreshSuccess)
                if (it.showEnd) homePageAdapter.loadMoreModule.loadMoreEnd()
                it.loadPageStatus?.value?.let { loadPageStatus ->
                    loadPageView.convert(
                        rootView,
                        loadPageStatus = loadPageStatus
                    )
                    homePageAdapter.setEmptyView(rootView)
                }
                it.showSuccess?.let { list ->
                    homePageAdapter.run {
                        loadMoreModule.isEnableLoadMore = false
                        if (it.isRefresh) setList(list) else addData(list)
                        loadMoreModule.isEnableLoadMore = true
                        loadMoreModule.loadMoreComplete()
                    }
                }
                it.showError?.let { errorMsg ->//加载失败处理
                    homePageAdapter.loadMoreModule.loadMoreFail()
                    SmartToast.show(errorMsg)
                }

            })

        }

    }

    override fun onLoadMore() {
        mViewModel.loadSquareArticleList(false)
    }


}