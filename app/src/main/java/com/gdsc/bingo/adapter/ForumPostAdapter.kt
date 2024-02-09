package com.gdsc.bingo.adapter


import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.gdsc.bingo.R
import com.gdsc.bingo.databinding.ComponentCardKomunitasBinding
import com.gdsc.bingo.model.Forums
import com.gdsc.bingo.model.User
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.storage.FirebaseStorage
import java.time.ZoneId

class ForumPostAdapter(
    private val context: Context,
    private val storage: FirebaseStorage,
    var actionComment : (Forums) -> Unit = {},
    var actionLike : (Forums) -> Unit = {},
    var actionVerticalButton : (Forums) -> Unit = {},
    var actionOpenDetail : (Forums) -> Unit = {}
) : ListAdapter<Forums, ForumPostAdapter.ForumPostViewHolder>(ForumsPostDiffUtil()) {
    class ForumsPostDiffUtil : DiffUtil.ItemCallback<Forums>() {
        override fun areItemsTheSame(oldItem: Forums, newItem: Forums): Boolean {
            return oldItem.referencePath == newItem.referencePath
        }

        override fun areContentsTheSame(oldItem: Forums, newItem: Forums): Boolean {
            return oldItem.toString() == newItem.toString()
        }

    }

    inner class ForumPostViewHolder(
        private val binding : ComponentCardKomunitasBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(forums: Forums) {
            Log.i("ForumPostAdapter", "Bind data to viewholder: \n\t${forums.toString()}")

            setupProfile(forums.author!!)
            setupPostTimestamp(forums.createdAt!!)
            setupThumbnail(forums.thumbnailPhotosUrl)
            setupPostTitle(forums.title!!)
            setupCommentAndLikeCount(forums.commentCount, forums.likeCount)
            setupCommentButton(forums)
            setupLikeButton(forums)
            setupVerticalButton(forums)
            setupOpenDetail(forums)
        }

        private fun setupOpenDetail(forums: Forums) {
            binding.componentCardKomunitasContainer.setOnClickListener {
                actionOpenDetail(forums)
                forums.logClickAction("setupOpenDetail")
            }
        }

        private fun setupVerticalButton(forums: Forums) {
            binding.componentCardKomunitasButtonMore.setOnClickListener {
                actionVerticalButton(forums)
                forums.logClickAction("setupVerticalButton")
            }
        }

        private fun setupLikeButton(forums: Forums) {
            binding.componentCardKomunitasButtonLike.setOnClickListener {
                actionLike(forums)
                forums.logClickAction("setupLikeButton")
            }
        }

        private fun setupCommentButton(forums: Forums) {
            binding.componentCardKomunitasButtonComment.setOnClickListener {
                actionComment(forums)
                forums.logClickAction("setupCommentButton")
            }
        }

        private fun setupCommentAndLikeCount(commentCount: Long, likeCount: Long) {
            val komen = context.resources.getString(R.string.komentar)
            val suka = context.resources.getString(R.string.suka)
            val text = "$commentCount $komen, $likeCount $suka"
            binding.componentCardKomunitasTextViewCommentLikeCount.text = text
        }


        private fun setupPostTitle(title: String) {
            binding.componentCardKomunitasTextViewTitle.text = title
        }

        /**
         * [setupThumbnail]
         *
         *
         * Tidak semua post memiliki thumbnail, jika tidak ada thumbnail maka view akan dihilangkan
         */
        private fun setupThumbnail(thumbnailPhotosUrl: String?) {
            binding.componentCardKomunitasImageViewPost.visibility = if (thumbnailPhotosUrl == null) View.GONE else View.VISIBLE
            if (thumbnailPhotosUrl == null) return

            storage.getReference(thumbnailPhotosUrl).downloadUrl.addOnSuccessListener {
                binding.componentCardKomunitasImageViewPost.load(thumbnailPhotosUrl) {
                    crossfade(true)
                    placeholder(R.drawable.ic_info_outline_48)
                    error(R.drawable.ic_info_outline_48)
                }
            }.addOnFailureListener {
                Log.e("ForumPostAdapter", "Error getting thumbnail", it)
            }
        }

        private fun setupPostTimestamp(createdAt: Timestamp) {
            val date = createdAt.toDate()

            val zonedDateTime = date.toInstant().atZone(ZoneId.systemDefault())
            val text = "${zonedDateTime.dayOfMonth} ${zonedDateTime.month} ${zonedDateTime.year}, ${zonedDateTime.hour}:${zonedDateTime.minute}"
            binding.componentCardKomunitasTextViewDateTime.text = text

        }

        private fun setupProfile(authorReference: DocumentReference) {
            authorReference.get().addOnSuccessListener {
                val user = it.toObject(User::class.java) ?: run {
                    Log.e("ForumPostAdapter", "User data is null")
                    return@addOnSuccessListener
                }

                loadUsername(user.username!!)
                loadProfilePicture(user.profilePicturePath)

            }.addOnFailureListener {
                Log.e("ForumPostAdapter", "Error getting user data in setupProfile", it)
            }
        }

        private fun loadProfilePicture(profilePicturePath: String?) {
            if (profilePicturePath == null) {
                Log.e("ForumPostAdapter", "Profile picture path is null")
                binding.componentCardKomunitasCardProfilImage.load(R.drawable.ic_person_24) {
                    transformations(CircleCropTransformation())
                }
                return
            }

            storage.getReference(profilePicturePath).downloadUrl.addOnSuccessListener {
                Log.i("ForumPostAdapter", "Profile picture loaded : $it")
                binding.componentCardKomunitasCardProfilImage.load(it) {
                    crossfade(true)
                    transformations(CircleCropTransformation())
                    placeholder(R.drawable.ic_person_24)
                    error(R.drawable.ic_person_24)
                }
            }.addOnFailureListener {
                Log.e("ForumPostAdapter", "Error getting profile picture", it)
            }
        }

        private fun loadUsername(username: String) {
            binding.componentCardKomunitasTextViewName.text = username
        }

    }

    private fun Forums.logClickAction(msg: String) {
        Log.i("ForumPostAdapter", "Successful run $msg" +
                "\n\tForum Detail :" +
                "\n\t\tTitle : ${this.title}" +
                "\n\t\tDatetime : ${this.createdAt?.toDate()}" +
                "\n\n---------------------------\n\n")
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForumPostViewHolder {
        val binding = ComponentCardKomunitasBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ForumPostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ForumPostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}