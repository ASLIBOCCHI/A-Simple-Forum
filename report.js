const fs = require("fs");

const text = fs.readFileSync("backup.txt", "utf-8");
const lines = text.split("\n");

let posts = [];
let currentPost = null;

lines.forEach(line => {
  line = line.trim();

  if (line.startsWith("Post by:")) {
    if (currentPost) posts.push(currentPost);

    currentPost = {
      author: line.replace("Post by:", "").trim(),
      content: "",
      replies: []
    };
  }

  else if (line.startsWith("Content:")) {
    currentPost.content = line.replace("Content:", "").trim();
  }

  else if (line.startsWith("- ")) {
    const parts = line.substring(2).split(":");
    const author = parts.shift().trim();
    const text = parts.join(":").trim();

    currentPost.replies.push({ author, text });
  }
});

if (currentPost) posts.push(currentPost);
posts.forEach((post, i) => {
  console.log(`\n=== POST ${i + 1} ===`);
  console.log(`Author : ${post.author}`);
  console.log(`Question: ${post.content}`);
  console.log("Replies:");

  post.replies.forEach((r, j) => {
    console.log(`${j + 1}. ${r.author}`);
    console.log(`   ${r.text}`);
  });
});


let csv = "Post Author,Question,Reply Author,Reply\n";

posts.forEach(post => {
  post.replies.forEach(r => {
    csv += `"${post.author}","${post.content}","${r.author}","${r.text}"\n`;
  });
});

fs.writeFileSync("report.csv", csv);