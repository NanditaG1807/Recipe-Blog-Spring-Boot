



    window.addEventListener('DOMContentLoaded', function () {
    const params = new URLSearchParams(window.location.search);

    if (params.has('error')) {
    alert('Email or password is incorrect. Please try again.');
}

    if (params.has('logout')) {
    alert('You have been logged out successfully.');
}
});
