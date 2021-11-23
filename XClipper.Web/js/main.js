AOS.init({
  duration: 800,
  easing: "slide",
  once: true,
});

history.scrollRestoration = "manual";

if (Number(window.screen.width < 600)) {
  const cards = document.getElementsByClassName("card");
  Array.from(cards).forEach((el) => {
    el.style.width = `${Number((window.screen.width - 54) / 16)}rem`;
  });
}

makeToActOnCard("freeCard", "freeCardHr");
// makeToActOnCard("standardCard", "standardCardHr");
makeToActOnCard("premiumCard", "premiumCardHr");

$(document).ready(function () {
  loadWindowsInformation();
  // loadAndroidInformation(); // TODO: Uncomment this when the app install should be shown from github releases.
});

function showModalDialog(
  title,
  message,
  buttonText = "OK",
  lottieAnimationPath = null
) {
  document.getElementById("dialog-title").innerHTML = title;
  document.getElementById("dialog-button").innerHTML = buttonText;
  const content = document.getElementById("dialog-content");
  content.innerHTML = "";

  if (lottieAnimationPath != null) {
    $("#dialog-modal").modal("show");

    const container = document.getElementById("lottie-container");
    container.innerHTML = "";
    container.className = "lottie-fadeOut-animation";

    lottie.loadAnimation({
      container: container,
      renderer: "svg",
      loop: false,
      autoplay: true,
      path: lottieAnimationPath,
    });

    container.addEventListener(
      "animationend",
      function (e) {
        content.innerHTML = message;
        content.className = "text-fadeIn-animation";
      },
      false
    );
  } else {
    content.innerHTML = message;
  }
}

/* function baseDialog() {
  showModalDialog(
    "Purchase Completed",
    "Restart the XClipper application on Windows to activate the license.",
    "Alright",
    "XClipper.Web/raw/premium_unlocked.json"
  );
} */

function hideCircle() {
  console.log("Ran");
}

function makeToActOnCard(cardElement, cardElementHr) {
  var card = document.getElementById(cardElement);
  var cardHr = document.getElementById(cardElementHr);
  card.addEventListener("mouseover", function () {
    cardHr.style.color = "#515b80";
    cardHr.style.backgroundColor = "#515b80";
  });
  card.addEventListener("mouseleave", function () {
    cardHr.style.color = "#2d303b";
    cardHr.style.backgroundColor = "#2d303b";
  });
}

function scrollToVideo() {
  scrollToSection("videoSection", "videoHr", 10);
}

function scrollToPricing() {
  scrollToSection("purchaseSection", "purchaseHr", 6);
}

function scrollToSection(elementId, secondElementId, offset) {
  var screenWidth = document.documentElement.clientWidth;
  if (screenWidth >= 992) {
    var element = document.getElementById(elementId);

    const elementRect = element.getBoundingClientRect();
    const absoluteElementTop = elementRect.top + window.pageYOffset;
    const middle = absoluteElementTop - window.innerHeight / offset;
    window.scrollTo(0, middle);
  } else {
    document.getElementById(secondElementId).scrollIntoView();
  }
}

function openGithub() {
  window.open("https://github.com/KaustubhPatange/XClipper");
}

function openScreenShots() {
  window.open("https://imgur.com/a/n0KJd3Q");
}

function openPrivacyPolicy() {
  let href = window.location.href;
  href = href.replace(/#(.*)/g, "").replace("index.html", "");
  window.open(href + "policy" + (href.includes(":") ? ".html" : ""));
}

function openDocs() {
  let href = window.location.href;
  href = href.replace(/#(.*)/g, "").replace("index.html", "");
  window.open(href + "docs");
}

function showContactForm() {
  document.getElementById("contact-button").disabled = false;
  document.getElementById("emailInput").value = "";
  document.getElementById("subjectInput").value = "";
  document.getElementById("messageTextArea").value = "";
  $("#contact-modal").modal("show");
}

function contactSubmit() {
  const fromEmail = document.getElementById("emailInput").value;
  const title = document.getElementById("subjectInput").value;
  const message = document.getElementById("messageTextArea").value;
  if (
    isEmptyOrSpaces(fromEmail) ||
    isEmptyOrSpaces(title) ||
    isEmptyOrSpaces(message)
  ) {
    showToast("One or more fields are empty!");
    return;
  }
  const params = {
    email: fromEmail,
    subject: title,
    body: message,
  };
  const xmlHttp = new XMLHttpRequest();
  xmlHttp.onreadystatechange = function () {
    if (xmlHttp.readyState == 4) {
      showToast(
        "Thank you for contacting, I'll reach out to you soon!",
        false,
        4000
      );
      $("#contact-modal").modal("hide");
    }
  };
  xmlHttp.open(
    "POST", // will be sent to xclipper.help@gmail.com
    "https://script.google.com/macros/s/AKfycbzuX490mM41ZO7TqzGAn6oAfC4HP6avThqrrO208nNiQYfRUTY/exec",
    true
  );
  xmlHttp.send(JSON.stringify(params));
  document.getElementById("contact-button").disabled = true;

  return false;
}

function isEmptyOrSpaces(str) {
  return str === null || str.match(/^ *$/) !== null;
}

function showToast(msg, error = true, timeSpan = 2500) {
  const options = {
    style: {
      main: {
        background: Boolean(error) ? "#ff5733" : " #0A8E72",
        "font-family": "roboto mono",
      },
    },
    settings: {
      duration: timeSpan,
    },
  };
  iqwerty.toast.toast(msg, options);
}

function downloadClick() {
  document.getElementById("download-popup").style.display = "block";
}

function downloadFocusOut() {
  document.getElementById("download-popup").style.display = "none";
}

async function loadWindowsInformation() {
  const options = {
    url: "https://api.github.com/repos/KaustubhPatange/XClipper/releases",
    method: "GET",
  };

  const response = await promisifiedRequest(options);
  const jObject = JSON.parse(response);
  for (var i = 0; i < jObject.length; i++) {
    const obj = jObject[i];
    var set = false;
    obj.assets.map((e) => {
      if (String(e.name).endsWith(".exe")) {
        document.getElementById("btn-window").href = e.browser_download_url;
        set = true;
      }
    });
    if (set) break;
  }
}

async function loadAndroidInformation() {
  const options = {
    url: "https://api.github.com/repos/KaustubhPatange/XClipper/releases",
    method: "GET",
  };

  const response = await promisifiedRequest(options);
  const jObject = JSON.parse(response);
  for (var i = 0; i < jObject.length; i++) {
    const obj = jObject[i];
    var set = false;
    obj.assets.map((e) => {
      if (String(e.name).endsWith(".apk")) {
        document.getElementById("btn-apk").href = e.browser_download_url;
        set = true;
      }
    });
    if (set) break;
  }
}
