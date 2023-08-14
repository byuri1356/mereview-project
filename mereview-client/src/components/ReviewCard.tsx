import React from "react";
import { Container, Row, Col } from "react-bootstrap";
import { ReviewCardInterface } from "./interface/ReviewCardInterface";
import "../styles/css/ReviewCard.css";
import { useNavigate } from "react-router-dom";

type Style = {
  [key: string]: string | number;
};

const ReviewCard = (props: ReviewCardInterface) => {
  const {
    className,
    reviewId,
    memberId,
    nickname,
    profileImageId,
    backgroundImageId,
    oneLineReview,
    funnyCount,
    usefulCount,
    dislikeCount,
    commentCount,
    movieTitle,
    releaseYear,
    movieGenre,
    createDate,
    recommend,
  } = props;
  const navigate = useNavigate();

  const handleClickReviewCard = (
    event: React.MouseEvent<HTMLParagraphElement>
  ) => {
    navigate(`/review/${reviewId}`);
  };

  const handleClickProfile = (
    event: React.MouseEvent<HTMLParagraphElement>
  ) => {
    event.stopPropagation();
    navigate(`/profile/${memberId}`);
  };

  const handleClickMovie = (event: React.MouseEvent<HTMLParagraphElement>) => {
    event.stopPropagation();
    console.log("Movie Name Clicked", movieTitle);
  };

  const cardStyle: Style = {};
  if (backgroundImageId) {
    cardStyle.backgroundImage = `url(${process.env.REACT_APP_API_URL}/image/download/backgrounds/${backgroundImageId})`;
  }

  const recommendStyle: Style = {};
  if (funnyCount + usefulCount + dislikeCount > 0) {
    recommendStyle.opacity =
      (funnyCount + usefulCount) / (funnyCount + usefulCount + dislikeCount);
  }

  const formattedCreateDate: Date = new Date(createDate);
  const year: number = formattedCreateDate.getFullYear();
  const month: string = String(formattedCreateDate.getMonth() + 1).padStart(
    2,
    "0"
  );
  const day: string = String(formattedCreateDate.getDate()).padStart(2, "0");
  const hour: string = String(formattedCreateDate.getHours()).padStart(2, "0");
  const minute: string = String(formattedCreateDate.getMinutes()).padStart(
    2,
    "0"
  );
  const genres: string = movieGenre.join(". ");
  const defaultProfileImage = "/testProfile.gif";

  return (
    <>
      <div
        className={`review-card ${className}`}
        style={cardStyle}
        onClick={handleClickReviewCard}
      >
        <div className="card-overlay">
          <Row>
            <Col md={"auto"} className="date">
              {year}-{month}-{day} {hour}:{minute}
            </Col>
            <Col className="evaluation">
              <span>재밌어요: {funnyCount}</span> |{" "}
              <span>유용해요: {usefulCount}</span> |{" "}
              <span>별로에요: {dislikeCount}</span> |{" "}
              <span>Comment: {commentCount}</span>
            </Col>
          </Row>
          <Row>
            <Col>
              <span className="one-line-review">{oneLineReview}</span>
            </Col>
          </Row>
          <div className="additional-info">
            <Row>
              <Col className="profile-container">
                <div className="profile-img">
                  <img
                    src={
                      profileImageId
                        ? `${process.env.REACT_APP_API_URL}/image/download/profiles/${profileImageId}`
                        : defaultProfileImage
                    }
                    alt="Profile"
                  />
                </div>
                <span className="nickname" onClick={handleClickProfile}>
                  {nickname}
                </span>
              </Col>
            </Row>
            <Row>
              <Col>
                <span className="movie-title" onClick={handleClickMovie}>
                  {movieTitle}
                </span>
              </Col>
            </Row>
            <Row>
              <Col>
                <span className="year-genres">
                  {releaseYear} | {genres}
                </span>
              </Col>
            </Row>
            <div className="recommend" style={recommendStyle}>
              {recommend ? (
                <img src="/ReviewCardDummy/thumbsup.png" alt="추천!!" />
              ) : (
                <img src="/ReviewCardDummy/thumbsdown.png" alt="비추!!" />
              )}
            </div>
          </div>
        </div>
      </div>
    </>
  );
};

export default ReviewCard;
