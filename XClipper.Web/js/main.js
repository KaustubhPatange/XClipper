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
  loadInformation();
});

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
  scrollToSection("videoSection", "videoHr");
}

function scrollToPricing() {
  scrollToSection("purchaseSection", "purchaseHr");
}

function scrollToSection(elementId, secondElementId) {
  var screenWidth = document.documentElement.clientWidth;
  if (screenWidth >= 992) {
    var element = document.getElementById(elementId);

    const elementRect = element.getBoundingClientRect();
    const absoluteElementTop = elementRect.top + window.pageYOffset;
    const middle = absoluteElementTop - window.innerHeight / 5;
    window.scrollTo(0, middle);
  } else {
    document.getElementById(secondElementId).scrollIntoView();
  }
}

function openGithub() {
  window.open("https://github.com/KaustubhPatange/XClipper");
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
    "POST",
    "https://script.google.com/macros/s/AKfycbzX3zKSJvupqu914uo-p4IVCPXFFjVidJ4aG7gaHYTWPe8Sgmqq/exec",
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

async function loadInformation() {
  const options = {
    url: "https://pastebin.com/raw/fnrK1Mcx", // TODO: Change to real update url
    method: "GET",
  };
  const response = await promisifiedRequest(options);
  const jObject = JSON.parse(response);

  document.getElementById("btn-window").href = jObject.Desktop.DownloadUri;
}
