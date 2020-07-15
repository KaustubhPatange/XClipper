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
makeToActOnCard("standardCard", "standardCardHr");
makeToActOnCard("premiumCard", "premiumCardHr");

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

function showAlert() {
  alert("Hello\nHow are you?");
}
