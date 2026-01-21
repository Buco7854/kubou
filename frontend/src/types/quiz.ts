export interface QuestionDTO {
  id: string;
  text: string;
  options: string[];
  correctAnswerIndex: number;
  tags: string[];
  difficultyLevel: number;
}

export interface QuizDTO {
  id: string;
  title: string;
  questions: QuestionDTO[];
}
